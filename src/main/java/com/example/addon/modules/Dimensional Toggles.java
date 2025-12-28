package com.yourname.basekeeper.modules;

import java.util.ArrayList;
import java.util.List;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DimensionToggles extends Module {
    private final SettingGroup sgOverworld = settings.createGroup("Overworld");
    private final SettingGroup sgNether = settings.createGroup("Nether");
    private final SettingGroup sgEnd = settings.createGroup("End");

    private final Setting<List<String>> overworldEnable = sgOverworld.add(
        new StringListSetting.Builder().name("enable").defaultValue(new ArrayList<>()).build()
    );
    private final Setting<List<String>> overworldDisable = sgOverworld.add(
        new StringListSetting.Builder().name("disable").defaultValue(new ArrayList<>()).build()
    );

    private final Setting<List<String>> netherEnable = sgNether.add(
        new StringListSetting.Builder().name("enable").defaultValue(new ArrayList<>()).build()
    );
    private final Setting<List<String>> netherDisable = sgNether.add(
        new StringListSetting.Builder().name("disable").defaultValue(new ArrayList<>()).build()
    );

    private final Setting<List<String>> endEnable = sgEnd.add(
        new StringListSetting.Builder().name("enable").defaultValue(new ArrayList<>()).build()
    );
    private final Setting<List<String>> endDisable = sgEnd.add(
        new StringListSetting.Builder().name("disable").defaultValue(new ArrayList<>()).build()
    );

    private RegistryKey<World> lastDim = null;

    public DimensionToggles(Category category) {
        super(category, "dimension-toggles", "Auto enables/disables modules based on dimension.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        RegistryKey<World> dim = mc.world.getRegistryKey();
        if (dim == lastDim) return;

        lastDim = dim;

        if (dim == World.OVERWORLD) apply(overworldEnable.get(), overworldDisable.get());
        else if (dim == World.NETHER) apply(netherEnable.get(), netherDisable.get());
        else if (dim == World.END) apply(endEnable.get(), endDisable.get());
    }

    private void apply(List<String> enable, List<String> disable) {
        for (String name : enable) setModule(name, true);
        for (String name : disable) setModule(name, false);
    }

    private void setModule(String raw, boolean on) {
        if (raw == null) return;
        String name = raw.trim();
        if (name.isEmpty()) return;

        Module m = Modules.get().get(name);
        if (m == null) return;

        if (on && !m.isActive()) m.toggle();
        if (!on && m.isActive()) m.toggle();
    }
}
