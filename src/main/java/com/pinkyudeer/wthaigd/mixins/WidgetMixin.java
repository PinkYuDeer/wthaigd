package com.pinkyudeer.wthaigd.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.pinkyudeer.wthaigd.gui.widget.IBorderAble;
import com.pinkyudeer.wthaigd.helper.render.GLDrawHelper;

@Mixin(Widget.class)
public abstract class WidgetMixin<W extends Widget<W>> implements IBorderAble<W> {

    @Unique
    public boolean wthaigd$shouldDrawBorder = false;
    @Unique
    public boolean wthaigd$shouldRoundedBorder = false;
    @Unique
    public int wthaigd$borderColor;
    @Unique
    public int wthaigd$borderSize;
    @Unique
    public int wthaigd$borderRadius;
    @Unique
    public int wthaigd$borderFlags;
    @Unique
    public int wthaigd$cornerFlags;

    @Shadow(remap = false)
    public abstract Area getArea();

    @Inject(method = "draw", at = @At("HEAD"), remap = false)
    public void wthaigd$draw(ModularGuiContext context, WidgetTheme widgetTheme, CallbackInfo ci) {
        if (wthaigd$shouldDrawBorder) {
            int width = this.getArea().width;
            int height = this.getArea().height;
            int x = 0;
            int y = 0;

            // 根据是否使用圆角边框选择绘制方法
            if (wthaigd$shouldRoundedBorder) {
                GLDrawHelper.drawRoundedBorder(
                    x,
                    y,
                    width,
                    height,
                    wthaigd$borderRadius,
                    wthaigd$borderColor,
                    wthaigd$borderSize,
                    wthaigd$borderFlags,
                    wthaigd$cornerFlags);
            } else {
                GLDrawHelper
                    .drawBorder(x, y, width, height, wthaigd$borderColor, wthaigd$borderSize, wthaigd$borderFlags);
            }
        }
    }

    @Unique
    @SuppressWarnings({ "unchecked", "DataFlowIssue" })
    public W wthaigd$border(int borderColor, int borderSize, int borderRadius, int borderFlags, int cornerFlags) {
        this.wthaigd$shouldDrawBorder = true;
        if (borderRadius > 0) {
            this.wthaigd$shouldRoundedBorder = true;
            this.wthaigd$borderRadius = borderRadius;
        }
        this.wthaigd$borderColor = borderColor;
        this.wthaigd$borderSize = borderSize;
        this.wthaigd$borderFlags = borderFlags;
        this.wthaigd$cornerFlags = cornerFlags;
        return (W) (Object) this;
    }
}
