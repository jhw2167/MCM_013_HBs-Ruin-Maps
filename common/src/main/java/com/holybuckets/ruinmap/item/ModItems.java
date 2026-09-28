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

    public static final Map<Rarity, DeferredObject<Item>> unwrappedRuinMap = new EnumMap<>(Rarity.class);
    public static final Map<Rarity, DeferredObject<Item>> ruinMap = new EnumMap<>(Rarity.class);

    public static void initialize(BalmItems items) {
        creativeModeTab = items.registerCreativeModeTab(
            () -> new ItemStack(unwrappedRuinMap.get(Rarity.LEGENDARY).get()), RUINMAP_TAB);

        for (Rarity rarity : Rarity.values()) {
            unwrappedRuinMap.put(rarity, items.registerItem(
                rl -> new UnwrappedRuinMapItem(rarity),
                id(unwrappedName(rarity)),
                RUINMAP_TAB));

            ruinMap.put(rarity, items.registerItem(
                rl -> new RuinMapItem(rarity),
                id(revealedName(rarity)),
                RUINMAP_TAB));
        }
    }

    public static Item unwrappedRuinMap(Rarity rarity) {
        DeferredObject<Item> obj = unwrappedRuinMap.get(rarity);
        return obj == null ? null : obj.get();
    }

    public static Item ruinMap(Rarity rarity) {
        DeferredObject<Item> obj = ruinMap.get(rarity);
        return obj == null ? null : obj.get();
    }

    public static String unwrappedName(Rarity rarity) {
        return "unwrapped_ruin_map_" + rarity.getSerializedName();
    }

    public static String revealedName(Rarity rarity) {
        return "ruin_map_" + rarity.getSerializedName();
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
