package com.holybuckets.ruinmap.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.core.ChunkExplorerManager;
import com.holybuckets.foundation.core.Rarity;
import com.holybuckets.foundation.datastore.DataStore;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.DatastoreSaveEvent;
import com.holybuckets.foundation.event.custom.PlayerNearStructureEvent;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.structure.StructureAPI;
import com.holybuckets.foundation.structure.StructureInfo;
import com.holybuckets.ruinmap.Constants;
import com.holybuckets.ruinmap.config.ModConfig;
import com.holybuckets.ruinmap.item.RuinMapItem;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import com.holybuckets.foundation.event.custom.TickType;
import com.mojang.datafixers.util.Pair;
import net.blay09.mods.balm.api.event.LevelLoadingEvent;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.holybuckets.foundation.CommonClass.MESSAGER;

public class MapManager {

    public static final String CLASS_ID = "013";
    public static final byte DEFAULT_MAP_SCALE = 4;

    private static final String DATA_KEY_DISCOVERED = "discoveredStructureChunks";

    private static final Map<ServerLevel, MapManager> MANAGERS = new HashMap<>();


    private final ServerLevel level;
    private final LongSet discoveredStructureChunks = new LongOpenHashSet();

    /** Structures that are primed for choosing */
    private final StructureAPI structureAPI;
    private final Map<Rarity, Queue<StructureInfo>> activeStructures;
    private final Map<Structure, List<StructureInfo>> cachedStructures;
    private final Set<Structure> structuresNotFoundInDim;   //blackList

    private static GeneralConfig CONFIG;
    private static ModConfig MOD_CONFIG;

    private MapManager(ServerLevel level) {
        this.level = level;
        this.structureAPI = new StructureAPI(level);
        this.structuresNotFoundInDim = new HashSet<>();
        this.activeStructures = new EnumMap<>(Rarity.class);
        this.cachedStructures = new HashMap<>();
    }

    public static void init(EventRegistrar reg) {
        reg.registerOnBeforeServerStarted(MapManager::onServerStart);
        reg.registerOnLevelLoad(MapManager::onLevelLoad);
        reg.registerOnDataSave(MapManager::onDataSave);
        reg.registerOnPlayerNearStructure(null, MapManager::onPlayerNearStructure);
        reg.registerOnServerTick(TickType.ON_120_TICKS, MapManager::on120Ticks);
    }

    private static MapManager initLevel(ServerLevel level) {
        if (!MANAGERS.containsKey(level)) {
            MANAGERS.put(level, new MapManager(level));
        }
        MANAGERS.get(level).load(CONFIG.getDataStore());
        MANAGERS.get(level).initStructures(level);
        return MANAGERS.get(level);
    }

    private void initStructures(ServerLevel level) {
        //1. scan the registry for structure not found in dim

        Registry<Structure> structRegistry = level.registryAccess().registry(Registries.STRUCTURE).orElseThrow();

        structuresNotFoundInDim.clear();
        for(Rarity rarity : Rarity.values()) {
            activeStructures.put(rarity, new LinkedList<>());
        }

        MOD_CONFIG.getAllStructureLocations().forEach(loc -> {
            if(structRegistry.containsKey(loc)) return;
            Structure missing = structRegistry.get(loc);
            if(missing != null) structuresNotFoundInDim.add(missing);
        });


    }

    private void buildActiveStructures(BlockPos playerPos) {
        for(Rarity rarity : Rarity.values()) {
            Set<ResourceLocation> rareStructures = MOD_CONFIG.getStructureLocations(rarity);
            List<StructureInfo> pool = structureAPI.nearestStructuresOfTypes(playerPos, rareStructures, 100);
            if(pool != null && !pool.isEmpty()) {
                var undiscovered  = pool.stream().filter(this::notDiscovered).toList();
                activeStructures.get(rarity).addAll(undiscovered);
            }
        }
    }

    private StructureInfo getUndiscoveredStructureNearPoint(BlockPos point, Rarity rarity) {
        Queue<StructureInfo> pool = activeStructures.get(rarity);
        StructureInfo closest = null;
        if (pool != null && !pool.isEmpty()) {
            int dist = Integer.MAX_VALUE;
            for (StructureInfo info : pool) {
                if (isDiscovered(info)) continue;
                int d = info.getOrigin().distManhattan(point);
                if (d < dist) {
                    closest = info;
                    dist = d;
                }
            }
        }
        return closest;
    }

    private void addPreferredExploreChunks()
    {
        Set<Structure> structures = MOD_CONFIG.getAllStructures();

        List<Structure> randomized = new ArrayList<>();
        for(Structure s : structures) {
            if(structuresNotFoundInDim.contains(s)) continue;
            if(cachedStructures.containsKey(s)) continue;

            List<StructureInfo> list = structureAPI.nearestStructuresOfType(BlockPos.ZERO, MOD_CONFIG.getStructureLoc(s), 100);
            if(list!=null && !list.isEmpty()) {
                cachedStructures.put(s, list); continue;
            }
            randomized.add(s);
        }

        if(randomized.isEmpty()) return;
        Collections.shuffle(randomized);

        Structure structToFind = randomized.get(0);
        HolderSet<Structure> target = MOD_CONFIG.getHolderSet(structToFind);
        if(target == null) {
            structuresNotFoundInDim.add(structToFind);
            return;
        }

        int radiusChunks = Math.max(1, ModConfig.MAX_STRUCTURE_LOC_DIST / 16); // 16 blocks per chunk
        Pair<BlockPos, ?> found = level.getChunkSource().getGenerator()
            .findNearestMapStructure(level, target, BlockPos.ZERO, radiusChunks, true);

        if(found==null || found.getFirst()==null) return;

        ChunkPos chunk = new ChunkPos(found.getFirst());
        ChunkExplorerManager.submitPriorityChunk(this.level, chunk);
    }


    public ServerLevel getLevel() {
        return this.level;
    }

    public boolean isDiscovered(BlockPos origin) {
        if (origin == null) return false;
        return discoveredStructureChunks.contains(new ChunkPos(origin).toLong());
    }

    public boolean isDiscovered(StructureInfo info) {
        return info != null && isDiscovered(info.getOrigin());
    }

    public boolean notDiscovered(StructureInfo info) {
        return info != null && !isDiscovered(info.getOrigin());
    }

    public boolean setDiscovered(BlockPos origin) {
        if (origin == null) return false;
        return discoveredStructureChunks.add(HBUtil.ChunkUtil.getChunkPos1DMap(new ChunkPos(origin)));
    }

    public boolean setDiscovered(StructureInfo info) {
        return info != null && setDiscovered(info.getOrigin());
    }

    public boolean isPooledStructure(ResourceLocation structure) {
        return ModConfig.getInstance().isPooledStructure(structure);
    }

    /** Parses activeStructures for closest structure to the player and creates a new map **/
    public ItemStack reveal(ServerPlayer player, ItemStack unwrapped, Rarity rarity)
    {
        buildActiveStructures(player.blockPosition());
        Queue<StructureInfo> pool = activeStructures.get(rarity);
        if (pool.isEmpty() ) return ItemStack.EMPTY;
        StructureInfo closest = pool.poll();

        Vec3 center = closest.getOrigin().getCenter();
        ItemStack map = new ItemStack(RuinMapItem.getMap(rarity));
        MapItemSavedData mapData = MapItemSavedData.createFresh(center.x, center.z,
            DEFAULT_MAP_SCALE, true, true, level.dimension());
        int key = level.getFreeMapId();
        level.setMapData(MapItem.makeKey(key), mapData);
        setDiscovered(closest);
        return map;
    }

    public Set<ResourceLocation> getStructurePool(Rarity rarity) {
        return ModConfig.getInstance().getStructureLocations(rarity);
    }

    public Set<Structure> getStructureObjectPool(Rarity rarity) {
        return ModConfig.getInstance().getStructures(rarity);
    }

    public void handleRevealFailed(ServerPlayer player, Rarity rarity) {
        MESSAGER.sendBottomActionHint(player, Component.translatable("message.hbs_ruinmap.reveal_failed").getString() );
    }



    //** EVENTS **//

    private void load(DataStore ds) {
        if (!GeneralConfig.getInstance().isServerSide()) return;
        JsonElement root = ds.getOrCreateLevelSaveData(Constants.MOD_ID, this.level).get(DATA_KEY_DISCOVERED);
        if (root == null || root.isJsonNull() || !root.isJsonArray()) return;

        discoveredStructureChunks.clear();
        for (JsonElement elem : root.getAsJsonArray()) {
            discoveredStructureChunks.add(elem.getAsLong());
        }
    }

    private void save(DataStore ds) {
        if (!GeneralConfig.getInstance().isServerSide()) return;
        JsonArray arr = new JsonArray();
        LongIterator it = discoveredStructureChunks.iterator();
        while (it.hasNext()) {
            arr.add(it.nextLong());
        }
        ds.getOrCreateLevelSaveData(Constants.MOD_ID, this.level).addProperty(DATA_KEY_DISCOVERED, arr);
    }

    private static void onServerStart(ServerStartingEvent event) {
        MANAGERS.clear();
        MOD_CONFIG = ModConfig.getInstance();
    }

    private static void onLevelLoad(LevelLoadingEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level )) return;
        CONFIG = GeneralConfig.getInstance();
        initLevel(level);
    }

    private static void onDataSave(DatastoreSaveEvent event) {
        for (MapManager manager : MANAGERS.values()) {
            manager.save(event.getDataStore());
        }
    }

    private static void onPlayerNearStructure(PlayerNearStructureEvent event) {
        StructureInfo info = event.getStructureInfo();
        if (info == null || info.getOrigin() == null) return;

        MapManager manager = MANAGERS.get(event.getPlayer().level());
        if (manager == null) return;
        if (manager.isPooledStructure(info.getStructureLocation()))
                manager.setDiscovered(info);
    }

    private static void on120Ticks(ServerTickEvent event) {

        for (MapManager manager : MANAGERS.values()) {
            if (manager == null) continue;
            manager.addPreferredExploreChunks();
        }
    }


    //** STATICS

    public static ItemStack getRevealedMap(ServerPlayer player, ItemStack unwrapped, Rarity rarity) {
        MapManager manager = MANAGERS.get(player.level());
        if (manager == null) return ItemStack.EMPTY;
        return manager.reveal(player, unwrapped, rarity);
    }

    public static void onRevealFailed(ServerPlayer player, Rarity rarity) {
        MapManager manager = MANAGERS.get(player.level());
        if (manager == null) return;
        manager.handleRevealFailed(player, rarity);
    }
}
