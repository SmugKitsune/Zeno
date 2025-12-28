package com.yourname.basekeeper.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;

public class VoidWarning extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> yLevel = sgGeneral.add(
        new IntSetting.Builder().name("warn-y").defaultValue(10).min(-64).max(320).build()
    );

    private final Setting<Integer> cooldownTicks = sgGeneral.add(
        new IntSetting.Builder().name("cooldown-ticks").defaultValue(60).min(1).max(600).build()
    );

    private int cd = 0;

    public VoidWarning(Category category) {
        super(category, "void-warning", "Warns you when you are below a Y level.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (cd > 0) cd--;

        if (mc.player.getY() <= yLevel.get() && cd == 0) {
            ChatUtils.warning("VoidWarning", "Low Y! (" + (int) mc.player.getY() + ")");
            cd = cooldownTicks.get();
        }
    }
}
