package com.yourname.basekeeper.modules;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

public class ProfileSwitcher extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> switchOnJoin = sgGeneral.add(
        new BoolSetting.Builder().name("switch-on-join").defaultValue(false).build()
    );

    private final Setting<String> joinProfile = sgGeneral.add(
        new StringSetting.Builder().name("join-profile").defaultValue("default").build()
    );

    private final Setting<Boolean> switchByDimension = sgGeneral.add(
        new BoolSetting.Builder().name("switch-by-dimension").defaultValue(false).build()
    );

    private final Setting<String> overworldProfile = sgGeneral.add(
        new StringSetting.Builder().name("overworld-profile").defaultValue("travel").build()
    );
    private final Setting<String> netherProfile = sgGeneral.add(
        new StringSetting.Builder().name("nether-profile").defaultValue("nether").build()
    );
    private final Setting<String> endProfile = sgGeneral.add(
        new StringSetting.Builder().name("end-profile").defaultValue("end").build()
    );

    private net.minecraft.registry.RegistryKey<World> lastDim = null;

    public ProfileSwitcher(Category category) {
        super(category, "profile-switcher", "Switches Meteor profiles on join or dimension change.");
    }

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        if (switchOnJoin.get()) loadProfile(joinProfile.get().trim());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!switchByDimension.get()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        var dim = mc.world.getRegistryKey();
        if (dim == lastDim) return;
        lastDim = dim;

        if (dim == World.OVERWORLD) loadProfile(overworldProfile.get().trim());
        else if (dim == World.NETHER) loadProfile(netherProfile.get().trim());
        else if (dim == World.END) loadProfile(endProfile.get().trim());
    }

    private void loadProfile(String profile) {
        if (profile == null || profile.isEmpty()) return;

        // Change this if your Meteor uses a different command:
        // Examples people use: "profiles load <name>" or "profile load <name>"
        try {
            Commands.get().dispatch("profiles load " + profile);
        } catch (Throwable t) {
            // fallback alternative name
            try { Commands.get().dispatch("profile load " + profile); } catch (Throwable ignored) {}
        }
    }
}
