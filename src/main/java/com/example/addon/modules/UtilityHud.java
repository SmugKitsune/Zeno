package com.yourname.basekeeper.hud;

import com.yourname.basekeeper.BaseKeeper;
import meteordevelopment.meteorclient.systems.hud.*;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import net.minecraft.client.MinecraftClient;

public class UtilityHud extends HudElement {
    public static final HudElementInfo<UtilityHud> INFO = new HudElementInfo<>(
        Hud.GROUP, "utility-hud", "Shows session stats (time, distance, deaths, pops, shulkers).", UtilityHud::new
    );

    public UtilityHud(Hud hud) {
        super(INFO, hud);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (MinecraftClient.getInstance().player == null) return;

        String time = BaseKeeper.STATE.sessionTimeString();
        String dist = String.format("%.0f", BaseKeeper.STATE.distanceTraveled);

        renderer.text("Session: " + time, x, y, hud.textColor.get());
        renderer.text("Distance: " + dist + " blocks", x, y + 10, hud.textColor.get());
        renderer.text("Deaths: " + BaseKeeper.STATE.deaths, x, y + 20, hud.textColor.get());
        renderer.text("Totems: " + BaseKeeper.STATE.totemsPopped, x, y + 30, hud.textColor.get());
        renderer.text("Shulkers: " + BaseKeeper.STATE.shulkersOpened, x, y + 40, hud.textColor.get());

        setSize(renderer.textWidth("Distance: " + dist + " blocks"), 50);
    }
}
