package com.holybuckets.ruinmap.config;

import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.core.Rarity;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.ruinmap.LoggerProject;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ModConfig {

    private static final String CLASS_ID = "013";
    private static ModConfig INSTANCE;

    private final Map<Rarity, Set<ResourceLocation>> structureLocations = new EnumMap<>(Rarity.class);
    private final Map<Rarity, Set<Structure>> structures = new EnumMap<>(Rarity.class);
    private final Set<ResourceLocation> allStructureLocations = new HashSet<>();
    private static final Map<Structure, ResourceLocation> structureToLocationMap = new HashMap<>();
    private Registry<Structure> structureRegistry;

    public static int MAX_STRUCTURE_LOC_DIST = 4096;

    private boolean hydrated = false;

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
        }
        return INSTANCE;
    }

    private ModConfig() {
        for (Rarity r : Rarity.values()) {
            structureLocations.put(r, new HashSet<>());
            structures.put(r, new HashSet<>());
        }
    }

    public static void init(EventRegistrar registrar) {
        INSTANCE = ModConfig.getInstance();
        registrar.registerOnBeforeServerStarted(ModConfig::onBeforeServerStarted, EventPriority.High);
        registrar.registerOnServerStopped(ModConfig::onServerStopped, EventPriority.Low);
    }

    public static RuinMapConfig getBalmConfig() {
        RuinMapConfig config = Balm.getConfig().getActiveConfig(RuinMapConfig.class);
        return (config != null) ? config : new RuinMapConfig();
    }

    public static boolean isDevMode() {
        return getBalmConfig().devMode;
    }


    public boolean isHydrated() {
        return hydrated;
    }

    public Set<ResourceLocation> getStructureLocations(Rarity rarity) {
        Set<ResourceLocation> set = structureLocations.get(rarity);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public Set<Structure> getStructures(Rarity rarity) {
        Set<Structure> set = structures.get(rarity);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public ResourceLocation getStructureLoc(Structure s) {
        return structureToLocationMap.get(s);
    }

    @Nullable
    public Registry<Structure> getStructureRegistry() {
        return structureRegistry;
    }

    @Nullable
    public TagKey<Structure> getStructureTag(ResourceLocation loc) {
        if (loc == null) return null;
        return TagKey.create(Registries.STRUCTURE, loc);
    }

    @Nullable
    public TagKey<Structure> getStructureTag(Structure s) {
        return getStructureTag(getStructureLoc(s));
    }

    @Nullable
    public Holder<Structure> getHolder(ResourceLocation loc) {
        if (loc == null || structureRegistry == null) return null;
        return structureRegistry.getHolder(ResourceKey.create(Registries.STRUCTURE, loc)).orElse(null);
    }

    @Nullable
    public Holder<Structure> getHolder(Structure s) {
        return getHolder(getStructureLoc(s));
    }

    @Nullable
    public HolderSet<Structure> getHolderSet(Structure s) {
        Holder<Structure> holder = getHolder(s);
        return holder == null ? null : HolderSet.direct(holder);
    }

    public Set<ResourceLocation> getAllStructureLocations() {
        return allStructureLocations;
    }

    public Set<Structure> getAllStructures() {
        return structures.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }


    public boolean isPooledStructure(ResourceLocation structureLoc) {
        if (structureLoc == null) return false;
        return allStructureLocations.contains(structureLoc);
    }

    public boolean isPooledStructure(Rarity rarity, ResourceLocation structureLoc) {
        if (structureLoc == null) return false;
        return getStructureLocations(rarity).contains(structureLoc);
    }

    private void onBeforeServerStarted() {
        RuinMapConfig activeConfig = getBalmConfig();
        hydrateStructures(activeConfig);

        LoggerProject.logInfo("013002",
            "Ruin Map structure pools hydrated: "
            + getStructureLocations(Rarity.COMMON).size() + " common, "
            + getStructureLocations(Rarity.RARE).size() + " rare, "
            + getStructureLocations(Rarity.EPIC).size() + " epic, "
            + getStructureLocations(Rarity.LEGENDARY).size() + " legendary");
    }

    private void hydrateStructures(RuinMapConfig activeConfig) {
        hydrated = false;

        for (Rarity r : Rarity.values()) {
            structureLocations.get(r).clear();
            structures.get(r).clear();
        }
        allStructureLocations.clear();

        structureToLocationMap.clear();

        Registry<Structure> registry = resolveStructureRegistry();
        this.structureRegistry = registry;
        if (registry == null) {
            LoggerProject.logError("013003",
                "Structure registry unavailable, Ruin Map structure pools left empty");
            return;
        }

        hydrateTier(Rarity.COMMON, activeConfig.commonStructures, registry);
        hydrateTier(Rarity.RARE, activeConfig.rareStructures, registry);
        hydrateTier(Rarity.EPIC, activeConfig.epicStructures, registry);
        hydrateTier(Rarity.LEGENDARY, activeConfig.legendaryStructures, registry);

        hydrated = true;
    }

    private void hydrateTier(Rarity rarity, List<String> configured, Registry<Structure> registry) {
        Set<ResourceLocation> locations = structureLocations.get(rarity);
        Set<Structure> resolved = structures.get(rarity);

        parseLocations(configured, registry, locations, rarity.getSerializedName() + "Structures", true);

        for (ResourceLocation loc : locations) {
            Structure structure = registry.get(loc);
            if (structure == null) continue;
            resolved.add(structure);
            structureToLocationMap.put(structure, loc);
        }

        allStructureLocations.addAll(locations);

        if (locations.isEmpty()) {
            LoggerProject.logWarning("013004",
                "Ruin Map tier '" + rarity.getSerializedName()
                + "' has no valid structures, maps of this tier will never resolve a target");
        }

        if (isDevMode()) {
            LoggerProject.logDebug("013005",
                "Tier '" + rarity.getSerializedName() + "' resolved to: " + locations);
        }
    }

    private void parseLocations(List<String> configured, Registry<Structure> registry,
                                Set<ResourceLocation> out, String fieldName, boolean requireRegistered) {
        if (configured == null) return;

        for (String structId : configured) {
            if (structId == null || structId.isBlank()) continue;

            ResourceLocation loc;
            try {
                loc = HBUtil.LOC(structId.trim());
            } catch (Exception e) {
                LoggerProject.logWarning("013006",
                    "Invalid structure id in " + fieldName + ": '" + structId + "'. " + e.getMessage());
                continue;
            }

            if (requireRegistered && !registry.containsKey(loc)) {
                LoggerProject.logWarning("013007",
                    "Structure '" + loc + "' in " + fieldName
                    + " is not present in the structure registry, skipping");
                continue;
            }

            out.add(loc);
        }
    }

    @Nullable
    private Registry<Structure> resolveStructureRegistry() {
        MinecraftServer server = GeneralConfig.getInstance().getServer();
        if (server == null) return null;
        try {
            return server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        } catch (Exception e) {
            LoggerProject.logError("013008", "Failed to access structure registry: " + e.getMessage());
            return null;
        }
    }

    private void onServerStopped() {
        structureRegistry = null;
        structureToLocationMap.clear();
        INSTANCE = null;
    }

    private static void onBeforeServerStarted(ServerStartingEvent event) {
        getInstance().onBeforeServerStarted();
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        getInstance().onServerStopped();
    }

    public Rarity getRarity(ResourceLocation loc) {
        for (Rarity r : Rarity.values()) {
            if (getStructureLocations(r).contains(loc)) {
                return r;
            }
        }
        return null;
    }
}
