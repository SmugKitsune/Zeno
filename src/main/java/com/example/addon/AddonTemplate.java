package com.yourname.basekeeper;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import com.yourname.basekeeper.modules.BaseTracker;
import com.yourname.basekeeper.modules.AutoTPA;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BaseKeeper extends MeteorAddon {
    @Override
    public void onInitialize() {
        Modules.get().add(new BaseTracker());
        Modules.get().add(new AutoTPA());
    }

    @Override
    public String getPackage() {
        return "com.yourname.basekeeper";
    }
}
