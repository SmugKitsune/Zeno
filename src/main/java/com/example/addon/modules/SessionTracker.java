package com.yourname.basekeeper.modules;

import com.yourname.basekeeper.BaseKeeper;
import meteordevelopment.meteorclient.events.entity.player.DeathEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class SessionTracker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> trackDistance = sgGeneral.add(
        new BoolSetting.Builder().name("track-distance").defaultValue(true).build()
    );

    public SessionTracker(Category category) {
        super(category, "session-tracker", "Tracks playtime, distance traveled, deaths, and other session stats.");
    }

    @Override
    public void onActivate() {
        BaseKeeper.STATE.sessionStartMs = System.currentTimeMillis();
        BaseKeeper.STATE.distanceTraveled = 0;
        BaseKeeper.STATE.deaths = 0;
        BaseKeeper.STATE.lastPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!trackDistance.get()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        BlockPos pos = mc.player.getBlockPos();
        if (BaseKeeper.STATE.lastPos != null) {
            double dx = pos.getX() - BaseKeeper.STATE.lastPos.getX();
            double dy = pos.getY() - BaseKeeper.STATE.lastPos.getY();
            double dz = pos.getZ() - BaseKeeper.STATE.lastPos.getZ();
            BaseKeeper.STATE.distanceTraveled += Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        BaseKeeper.STATE.lastPos = pos;
    }

    @EventHandler
    private void onDeath(DeathEvent event) {
        BaseKeeper.STATE.deaths++;
    }
}
