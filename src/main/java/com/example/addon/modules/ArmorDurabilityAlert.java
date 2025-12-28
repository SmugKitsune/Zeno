package com.yourname.basekeeper.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class ArmorDurabilityAlert extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> warnPercent = sgGeneral.add(
        new IntSetting.Builder().name("warn-percent").defaultValue(15).min(1).max(99).build()
    );

    private final Setting<Integer> everyTicks = sgGeneral.add(
        new IntSetting.Builder().name("check-every-ticks").defaultValue(40).min(1).max(200).build()
    );

    private int t = 0;

    public ArmorDurabilityAlert(Category category) {
        super(category, "armor-alert", "Warns when armor durability is low.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (++t < everyTicks.get()) return;
        t = 0;

        // armor: boots, leggings, chest, helmet
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack == null || stack.isEmpty() || !stack.isDamageable()) continue;

            int max = stack.getMaxDamage();
            int left = max - stack.getDamage();
            int pct = (int) Math.round((left * 100.0) / max);

            if (pct <= warnPercent.get()) {
                ChatUtils.warning("ArmorAlert", stack.getName().getString() + " durability low: " + pct + "%");
            }
        }
    }
}
