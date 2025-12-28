package com.yourname.basekeeper.modules;

import java.util.ArrayList;
import java.util.List;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.*;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;

public class KeybindCombos extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> key = sgGeneral.add(
        new KeybindSetting.Builder()
            .name("key")
            .description("Key to toggle the combo.")
            .defaultValue(Keybind.none())
            .build()
    );

    private final Setting<List<String>> modules = sgGeneral.add(
        new StringListSetting.Builder()
            .name("modules")
            .description("Module names to toggle (exact Meteor module names).")
            .defaultValue(new ArrayList<>(List.of("Elytra Fly", "No Fall")))
            .build()
    );

    private final Setting<Boolean> enableOnly = sgGeneral.add(
        new BoolSetting.Builder()
            .name("enable-only")
            .description("If true: only enables (won't disable).")
            .defaultValue(false)
            .build()
    );

    private boolean wasDown = false;

    public KeybindCombos(Category category) {
        super(category, "keybind-combo", "Toggles a list of modules with one keybind.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean down = key.get().isPressed();
        if (down && !wasDown) runCombo();
        wasDown = down;
    }

    private void runCombo() {
        for (String raw : modules.get()) {
            if (raw == null) continue;
            String name = raw.trim();
            if (name.isEmpty()) continue;

            Module m = Modules.get().get(name);
            if (m == null) continue;

            if (enableOnly.get()) {
                if (!m.isActive()) m.toggle();
            } else {
                m.toggle();
            }
        }
    }
}
