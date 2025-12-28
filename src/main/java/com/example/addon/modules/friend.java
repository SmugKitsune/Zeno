package com.yourname.basekeeper.modules;

import java.util.ArrayList;
import java.util.List;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;

public class AutoFriend extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> names = sgGeneral.add(
        new StringListSetting.Builder()
            .name("names")
            .description("Player names to add to your Meteor friends list when the module is enabled.")
            .defaultValue(new ArrayList<>(List.of("Ubuntu_", "clutchxz")))
            .build()
    );

    private final Setting<Boolean> disableAfter = sgGeneral.add(
        new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
            .name("disable-after")
            .description("Turn off automatically after adding friends once.")
            .defaultValue(true)
            .build()
    );

    public AutoFriend(Category category) {
        super(category, "auto-friend", "Adds a configurable list of players to your Meteor friends list.");
    }

    @Override
    public void onActivate() {
        boolean changed = false;

        for (String raw : names.get()) {
            if (raw == null) continue;
            String name = raw.trim();
            if (name.isEmpty()) continue;

            if (Friends.get().get(name) == null) {
                Friends.get().add(new Friend(name));
                changed = true;
            }
        }

        if (changed) Friends.get().save();

        if (disableAfter.get()) toggle();
    }
}
