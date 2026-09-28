package com.holybuckets.ruinmap.item;

import com.holybuckets.foundation.core.Rarity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IRuinMap {

    String TAG_RARITY = "RuinMapRarity";
    String TAG_TARGET = "RuinMapTarget";
    String TAG_STRUCTURE = "RuinMapStructure";

    Rarity getRarity();

    static Rarity getRarity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_RARITY)) {
            return Rarity.byName(tag.getString(TAG_RARITY));
        }
        if (stack.getItem() instanceof IRuinMap map) {
            return map.getRarity();
        }
        return Rarity.DEFAULT;
    }

    static void setRarity(ItemStack stack, Rarity rarity) {
        if (rarity == null) return;
        stack.getOrCreateTag().putString(TAG_RARITY, rarity.getSerializedName());
    }

    @Nullable
    static BlockPos getTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_TARGET)) return null;
        return BlockPos.of(tag.getLong(TAG_TARGET));
    }

    static void setTarget(ItemStack stack, BlockPos target) {
        if (target == null) return;
        stack.getOrCreateTag().putLong(TAG_TARGET, target.asLong());
    }

    @Nullable
    static ResourceLocation getStructureId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_STRUCTURE)) return null;
        return new ResourceLocation(tag.getString(TAG_STRUCTURE));
    }

    static void setStructureId(ItemStack stack, ResourceLocation id) {
        if (id == null) return;
        stack.getOrCreateTag().putString(TAG_STRUCTURE, id.toString());
    }
}
