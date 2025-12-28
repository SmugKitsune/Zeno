package com.yourname.basekeeper.modules;

import com.yourname.basekeeper.BaseKeeper;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;

public class ShulkerCounter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chatNotify = sgGeneral.add(
        new BoolSetting.Builder().name("chat-notify").defaultValue(false).build()
    );

    public ShulkerCounter(Category category) {
        super(category, "shulker-counter", "Counts shulker boxes you open this session.");
    }

    @EventHandler
    private void onOpen(OpenScreenEvent event) {
        if (event.screen instanceof ShulkerBoxScreen) {
            BaseKeeper.STATE.shulkersOpened++;
            if (chatNotify.get()) ChatUtils.info("ShulkerCounter", "Opened shulker. Total: " + BaseKeeper.STATE.shulkersOpened);
        }
    }
}
