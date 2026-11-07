package com.holybuckets.ruinmap.item;


import com.holybuckets.ruinmap.Constants;
import com.holybuckets.foundation.core.Rarity;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.item.BalmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {

    public static final ResourceLocation RUINMAP_TAB = id(Constants.MOD_ID);
    public static DeferredObject<CreativeModeTab> creativeModeTab;

    public static final Map<Rarity, DeferredObject<Item>> ruinMaps = new EnumMap<>(Rarity.class);

    public static void initialize(BalmItems items) {
        //creativeModeTab = items.registerCreativeModeTab(() -> new ItemStack(ruinMaps.get(Rarity.LEGENDARY).get()), RUINMAP_TAB);

        for (Rarity rarity : Rarity.values()) {
            ruinMaps.put(rarity, items.registerItem(
                rl -> new RuinMap(rarity),
                id(mapName(rarity)),
                RUINMAP_TAB));
        }
        FoundationCreativeTab.register(Constants.MOD_ID, Constants.MOD_NUM, () -> new Item[]{{
            ruinMaps.get(Rarity.COMMON).get(),
            ruinMaps.get(Rarity.UNCOMMON).get(),
            ruinMaps.get(Rarity.RARE).get(),
            ruinMaps.get(Rarity.EPIC).get(),
            ruinMaps.get(Rarity.LEGENDARY).get()
        });
    }

    public static Item ruinMap(Rarity rarity) {
        DeferredObject<Item> obj = ruinMaps.get(rarity);
        return obj == null ? null : obj.get();
    }

    public static String mapName(Rarity rarity) {
        return "ruin_map_" + rarity.getSerializedName();
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
