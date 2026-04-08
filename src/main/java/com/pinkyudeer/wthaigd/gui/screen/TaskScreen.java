package com.pinkyudeer.wthaigd.gui.screen;

import javax.annotation.Nonnull;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.gui.panel.MainPanel;
import com.pinkyudeer.wthaigd.render.BlurHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;

@SideOnly(Side.CLIENT)
public class TaskScreen extends CustomModularScreen {

    private static final long FADE_IN_MS = 300L;
    private static final long FADE_OUT_MS = 200L;

    private long openTime = -1;
    @Getter
    private boolean closing = false;
    private long closeStartTime = -1;
    private Runnable pendingClose;

    public TaskScreen() {
        super(Wthaigd.MODID);
    }

    @Override
    public @Nonnull ModularPanel buildUI(ModularGuiContext context) {
        return new MainPanel(this);
    }

    @Override
    public void drawScreen() {
        float alpha = calculateAlpha();
        BlurHandler.renderBlurredBackground(alpha);
        super.drawScreen();
    }

    private float calculateAlpha() {
        long now = System.currentTimeMillis();
        if (openTime < 0) openTime = now;

        if (closing) {
            float progress = (float) (now - closeStartTime) / FADE_OUT_MS;
            if (progress >= 1.0f) {
                closing = false;
                if (pendingClose != null) {
                    pendingClose.run();
                } else {
                    MCHelper.closeScreen();
                }
                return 0f;
            }
            return 1f - easeOut(progress);
        }

        float progress = (float) (now - openTime) / FADE_IN_MS;
        return easeOut(Math.min(1f, progress));
    }

    private static float easeOut(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    public void startClosing(Runnable onComplete) {
        if (!closing) {
            closing = true;
            closeStartTime = System.currentTimeMillis();
            pendingClose = onComplete;
        }
    }

    @Override
    public boolean doesPauseGame() {
        return false;
    }
}
