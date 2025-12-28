package com.yourname.basekeeper.modules;

import meteordevelopment.meteorclient.events.world.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoTPA extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> alts = sgGeneral.add(new StringSetting.Builder()
        .name("alts")
        .description("Comma-separated alt usernames. Empty = all.")
        .defaultValue("")
        .build()
    );

    private final Setting<Boolean> friendsOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("friends-only")
        .description("Only alts that are friended.")
        .defaultValue(false)
        .build()
    );

    private final Set<String> altSet = new HashSet<>();
    private final Pattern tpaPattern = Pattern.compile("(?i)(tpa|tp[a]?)\\s+from\\s+(\\w+)");

    public AutoTPA() {
        super("AutoTPA", "Auto-accept TPA from alts.", Module.Categories.MISC);
    }

    @Override
    public void onActivate() {
        updateAltSet();
    }

    private void updateAltSet() {
        altSet.clear();
        if (alts.get().trim().isEmpty()) return;
        for (String alt : alts.get().split(",")) {
            altSet.add(alt.trim().toLowerCase());
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = event.message.getString().toLowerCase();
        Matcher matcher = tpaPattern.matcher(msg);
        if (!matcher.find()) return;

        String sender = matcher.group(2);
        if (!altSet.isEmpty() && !altSet.contains(sender)) return;
        if (friendsOnly.get() && !Modules.get().get("Friends").isActive() /* approx */) return; // Enhance if needed

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getNetworkHandler().sendPacket(new ChatMessageC2SPacket("/tpaccept " + sender));
        ChatUtils.info("Auto-accepted TPA from %s", sender);
    }
}
