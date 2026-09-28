package com.holybuckets.ruinmap.item;

import com.holybuckets.ruinmap.Constants;
import com.holybuckets.ruinmap.core.MapManager;
import com.holybuckets.foundation.core.Rarity;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class UnwrappedRuinMapItem extends Item implements IRuinMap {

    private final Rarity rarity;

    public UnwrappedRuinMapItem(Rarity rarity) {
        super(Balm.getItems().itemProperties().stacksTo(1));
        this.rarity = rarity;
    }

    @Override
    public Rarity getRarity() {
        return this.rarity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel sl)) return InteractionResultHolder.pass(stack);

        Rarity r = IRuinMap.getRarity(stack);
        ItemStack revealed = MapManager.getRevealedMap(sp, stack, r);

        if (revealed.isEmpty()) {
            MapManager.onRevealFailed(sp, r);
            return InteractionResultHolder.fail(stack);
        }

        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, revealed);
        } else if (!player.getInventory().add(revealed)) {
            player.drop(revealed, false);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Rarity r = IRuinMap.getRarity(stack);
        tooltip.add(Component.translatable(r.getTranslationKey(Constants.MOD_ID)).withStyle(r.getColor()));
        tooltip.add(Component.translatable("item." + Constants.MOD_ID + ".unwrapped_ruin_map.desc")
            .withStyle(ChatFormatting.GRAY));
    }
}
