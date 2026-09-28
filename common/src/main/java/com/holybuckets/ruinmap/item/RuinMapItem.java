package com.holybuckets.ruinmap.item;

import com.holybuckets.ruinmap.Constants;
import com.holybuckets.ruinmap.core.MapManager;
import com.holybuckets.foundation.core.Rarity;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RuinMapItem extends MapItem implements IRuinMap {

    private final Rarity rarity;

    public static Item getMap(Rarity rarity) {
        return ModItems.ruinMap.get(rarity).get();
    }

    public static Item getUnwrapped(Rarity rarity) {
        return ModItems.unwrappedRuinMap.get(rarity).get();
    }

    public RuinMapItem(Rarity rarity) {
        super(Balm.getItems().itemProperties().stacksTo(1));
        this.rarity = rarity;
    }

    @Override
    public Rarity getRarity() {
        return this.rarity;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        Rarity r = IRuinMap.getRarity(stack);
        tooltip.add(Component.translatable(r.getTranslationKey(Constants.MOD_ID)).withStyle(r.getColor()));

        ResourceLocation structure = IRuinMap.getStructureId(stack);
        if (structure != null) {
            tooltip.add(Component.translatable("item." + Constants.MOD_ID + ".ruin_map.structure",
                Component.translatable(structure.toLanguageKey("structure"))).withStyle(ChatFormatting.GRAY));
        }

        /*BlockPos target = IRuinMap.getTarget(stack);
        if (target != null && flag.isAdvanced()) {
            tooltip.add(Component.translatable("item." + Constants.MOD_ID + ".ruin_map.target",
                target.getX(), target.getZ()).withStyle(ChatFormatting.DARK_GRAY));
        }*/
    }
}
