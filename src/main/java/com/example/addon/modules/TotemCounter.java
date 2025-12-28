package com.yourname.basekeeper.modules;

import com.yourname.basekeeper.BaseKeeper;
import meteordevelopment.meteorclient.events.game.ReceivePacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class TotemCounter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chatNotify = sgGeneral.add(
        new BoolSetting.Builder().name("chat-notify").defaultValue(true).build()
    );

    public TotemCounter(Category category) {
        super(category, "totem-counter", "Counts your totem pops this session.");
    }

    @EventHandler
    private void onPacket(ReceivePacketEvent event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;

        // 35 is commonly used for totem pop status
        if (p.getStatus() != 35) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        if (p.getEntity(mc.world) == mc.player) {
            BaseKeeper.STATE.totemsPopped++;
            if (chatNotify.get()) ChatUtils.info("TotemCounter", "Totem popped! Total: " + BaseKeeper.STATE.totemsPopped);
        }
    }
}
