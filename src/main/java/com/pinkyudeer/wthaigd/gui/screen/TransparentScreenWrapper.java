package com.pinkyudeer.wthaigd.gui.screen;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TransparentScreenWrapper extends GuiScreenWrapper {

    public TransparentScreenWrapper(ModularScreen screen) {
        super(screen);
    }

    @Override
    public void drawWorldBackground(int tint) {
        // Skip MUI2's default dark gradient overlay.
        // We render our own Gaussian blur background in TaskScreen.drawScreen().
    }
}
