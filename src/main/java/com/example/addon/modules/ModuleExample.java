package com.yourname.basekeeper.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.WaypointManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.waypoint.Waypoint;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BaseTracker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> webhookUrl = sgGeneral.add(new StringSetting.Builder()
        .name("webhook-url")
        .description("Discord webhook to ping coords.")
        .defaultValue("https://discord.com/api/webhooks/1454920623280099562/BA9zNvQxo4PrxT3S12wLsVd7-ui8aJtaW1f-KRQh5yL8u1J42D5Zh2TUmufM4IgOK8TK")
        .build()
    );

    private final Setting<Integer> minContainers = sgGeneral.add(new IntSetting.Builder()
        .name("min-containers")
        .description("Min containers to flag base (~2 per dub). Default 10 = >5 dubs.")
        .defaultValue(10)
        .sliderMin(5)
        .sliderMax(50)
        .build()
    );

    private final Setting<Integer> chunkRadius = sgGeneral.add(new IntSetting.Builder()
        .name("chunk-radius")
        .description("Chunks around you to scan.")
        .defaultValue(3)
        .sliderMin(1)
        .sliderMax(8)
        .build()
    );

    private final Setting<Long> scanDelayMs = sgGeneral.add(new Setting<>(new LongSetting.Builder()
        .name("scan-delay-ms")
        .description("Delay between full scans.")
        .defaultValue(1000L)
        .sliderMin(500L)
        .sliderMax(5000L)
        .build())
    );

    private final List<BlockPos> containers = new ArrayList<>();
    private long lastScan = 0;

    public BaseTracker() {
        super("BaseTracker", "Scans for base storage > threshold, pings webhook + waypoint.", Module.Categories.WORLD);
    }

    @Override
    public void onActivate() {
        containers.clear();
        ChatUtils.info("Scanning started...");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        long now = System.currentTimeMillis();
        if (now - lastScan < scanDelayMs.get()) return;

        scan();
        lastScan = now;
    }

    private void scan() {
        containers.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        World world = mc.world;
        ChunkPos center = new ChunkPos(mc.player.getBlockPos());
        Set<Block> containerBlocks = getContainerBlocks();

        for (int dx = -chunkRadius.get(); dx <= chunkRadius.get(); dx++) {
            for (int dz = -chunkRadius.get(); dz <= chunkRadius.get(); dz++) {
                WorldChunk chunk = world.getChunk(center.x + dx, center.z + dz);
                if (chunk != null) {
                    BlockPos.Mutable mutable = new BlockPos.Mutable();
                    for (int x = chunk.getPos().getStartX(); x <= chunk.getPos().getEndX(); x++) {
                        for (int z = chunk.getPos().getStartZ(); z <= chunk.getPos().getEndZ(); z++) {
                            for (int y = world.getBottomY(); y < world.getTopY(); y++) {
                                mutable.set(x, y, z);
                                BlockState state = chunk.getBlockState(mutable);
                                if (containerBlocks.contains(state.getBlock())) {
                                    containers.add(mutable.toImmutable());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (containers.size() > minContainers.get()) {
            BlockPos avgPos = getAveragePos(containers);
            notifyBase(avgPos, containers.size());
        }
    }

    private Set<Block> getContainerBlocks() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(Blocks.CHEST);
        blocks.add(Blocks.TRAPPED_CHEST);
        blocks.add(Blocks.BARREL);
        blocks.add(Blocks.SHULKER_BOX);
        blocks.add(Blocks.WHITE_SHULKER_BOX);
        blocks.add(Blocks.ORANGE_SHULKER_BOX);
        blocks.add(Blocks.MAGENTA_SHULKER_BOX);
        blocks.add(Blocks.LIGHT_BLUE_SHULKER_BOX);
        blocks.add(Blocks.YELLOW_SHULKER_BOX);
        blocks.add(Blocks.LIME_SHULKER_BOX);
        blocks.add(Blocks.PINK_SHULKER_BOX);
        blocks.add(Blocks.GRAY_SHULKER_BOX);
        blocks.add(Blocks.LIGHT_GRAY_SHULKER_BOX);
        blocks.add(Blocks.CYAN_SHULKER_BOX);
        blocks.add(Blocks.PURPLE_SHULKER_BOX);
        blocks.add(Blocks.BLUE_SHULKER_BOX);
        blocks.add(Blocks.BROWN_SHULKER_BOX);
        blocks.add(Blocks.GREEN_SHULKER_BOX);
        blocks.add(Blocks.RED_SHULKER_BOX);
        blocks.add(Blocks.BLACK_SHULKER_BOX);
        // Add ENDER_CHEST? Uncomment: blocks.add(Blocks.ENDER_CHEST);
        return blocks;
    }

    private BlockPos getAveragePos(List<BlockPos> posList) {
        long x = 0, y = 0, z = 0;
        for (BlockPos pos : posList) {
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
        }
        return new BlockPos(x / posList.size(), y / posList.size(), z / posList.size());
    }

    private void notifyBase(BlockPos pos, int count) {
        String msg = String.format("🚨 Base/Stash found @ %d %d %d (%d containers / ~%d dubs)", pos.getX(), pos.getY(), pos.getZ(), count, count / 2);
        ChatUtils.infoPrefix("BaseTracker", msg);

        // Webhook
        if (!webhookUrl.get().trim().isEmpty()) {
            String json = "{\"content\":\"" + msg.replace("\"", "\\\"") + "\"}";
            CompletableFuture.runAsync(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl.get().trim()))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    // Silent fail
                }
            });
        }

        // Waypoint
        Waypoint waypoint = new Waypoint("Base (" + count + " cont)", pos, 0xFF00FF);
        Modules.get().get(WaypointManager.class).add(waypoint);
    }
}


