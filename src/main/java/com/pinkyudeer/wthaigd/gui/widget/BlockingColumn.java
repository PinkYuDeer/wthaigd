package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.widgets.layout.Column;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A Column that blocks click-through, preventing mouse events from reaching widgets
 * below it in the render order. Used for dropdown/overlay menus.
 */
@SideOnly(Side.CLIENT)
public class BlockingColumn extends Column {

    @Override
    public boolean canClickThrough() {
        return false;
    }

    @Override
    public boolean canHoverThrough() {
        return false;
    }
}
