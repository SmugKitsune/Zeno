package com.yourname.basekeeper.modules;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ContainerSnapshot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> key = sgGeneral.add(
        new KeybindSetting.Builder().name("snapshot-key").defaultValue(Keybind.none()).build()
    );

    private final Setting<String> folder = sgGeneral.add(
        new StringSetting.Builder().name("folder").defaultValue("basekeeper-snapshots").build()
    );

    private boolean wasDown = false;

    public ContainerSnapshot(Category category) {
        super(category, "container-snapshot", "Press a key to save the open container contents to a text file.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean down = key.get().isPressed();
        if (down && !wasDown) snapshot();
        wasDown = down;
    }

    private void snapshot() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.currentScreen instanceof HandledScreen<?> hs)) {
            ChatUtils.warning("Snapshot", "Open a container first.");
            return;
        }

        ScreenHandler sh = hs.getScreenHandler();
        StringBuilder sb = new StringBuilder();

        sb.append("Snapshot: ").append(LocalDateTime.now()).append("\n");
        sb.append("Screen: ").append(hs.getTitle().getString()).append("\n\n");

        // Slots include player inventory too; label them by index
        for (int i = 0; i < sh.slots.size(); i++) {
            ItemStack st = sh.getSlot(i).getStack();
            if (st == null || st.isEmpty()) continue;
            sb.append("#").append(i).append("  ")
              .append(st.getCount()).append("x ")
              .append(st.getName().getString())
              .append("\n");
        }

        try {
            Path dir = Paths.get(folder.get());
            Files.createDirectories(dir);

            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path out = dir.resolve("snapshot_" + ts + ".txt");

            Files.writeString(out, sb.toString(), StandardOpenOption.CREATE_NEW);
            ChatUtils.info("Snapshot", "Saved: " + out.toAbsolutePath());
        } catch (IOException e) {
            ChatUtils.error("Snapshot", "Failed to save snapshot: " + e.getMessage());
        }
    }
}
