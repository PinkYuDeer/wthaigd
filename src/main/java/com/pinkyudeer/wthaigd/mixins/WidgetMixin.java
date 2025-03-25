package com.pinkyudeer.wthaigd.mixins;

import net.minecraft.client.gui.Gui;

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

@Mixin(Widget.class)
public abstract class WidgetMixin<W extends Widget<W>> implements IBorderAble<W> {

    @Unique
    public boolean wthaigd$shouldDrawBorder = false;
    @Unique
    public int wthaigd$borderColor = 0xFF808080;
    @Unique
    public int wthaigd$borderSize = 1;
    @Unique
    public boolean wthaigd$isInnerBorder = true;

    @Shadow(remap = false)
    public abstract Area getArea();

    @Inject(method = "draw", at = @At("HEAD"), remap = false)
    public void wthaigd$draw(ModularGuiContext context, WidgetTheme widgetTheme, CallbackInfo ci) {
        if (wthaigd$shouldDrawBorder) {
            // 内边框：绘制在 widget 内部
            int width = this.getArea().width;
            int height = this.getArea().height;
            int x = 0;
            int y = 0;
            if (wthaigd$isInnerBorder) {
                Gui.drawRect(x, y, x + width, y + wthaigd$borderSize, wthaigd$borderColor); // 上边
                Gui.drawRect(x, y + height - wthaigd$borderSize, x + width, y + height, wthaigd$borderColor); // 下边
                Gui.drawRect(
                    x,
                    y + wthaigd$borderSize,
                    x + wthaigd$borderSize,
                    y + height - wthaigd$borderSize,
                    wthaigd$borderColor); // 左边
                Gui.drawRect(
                    x + width - wthaigd$borderSize,
                    y + wthaigd$borderSize,
                    x + width,
                    y + height - wthaigd$borderSize,
                    wthaigd$borderColor); // 右边
            } else {
                // 外边框：绘制在 widget 外部
                Gui.drawRect(
                    x - wthaigd$borderSize,
                    y - wthaigd$borderSize,
                    x + width + wthaigd$borderSize,
                    y,
                    wthaigd$borderColor); // 上边
                Gui.drawRect(
                    x - wthaigd$borderSize,
                    y + height,
                    x + width + wthaigd$borderSize,
                    y + height + wthaigd$borderSize,
                    wthaigd$borderColor); // 下边
                Gui.drawRect(x - wthaigd$borderSize, y, x, y + height, wthaigd$borderColor); // 左边
                Gui.drawRect(x + width, y, x + width + wthaigd$borderSize, y + height, wthaigd$borderColor); // 右边
            }
        }
    }

    @Unique
    @SuppressWarnings({ "unchecked", "DataFlowIssue" })
    public W wthaigd$border(int wthaigd$borderColor, int wthaigd$borderSize, boolean isInnerBorder) {
        this.wthaigd$shouldDrawBorder = true;
        this.wthaigd$borderColor = wthaigd$borderColor;
        this.wthaigd$borderSize = wthaigd$borderSize;
        this.wthaigd$isInnerBorder = isInnerBorder;
        return (W) (Object) this;
    }

    @Unique
    public W wthaigd$border() {
        return wthaigd$border(0xFF808080, 1, true); // 默认参数
    }

    @Unique
    public W wthaigd$border(boolean isInnerBorder) {
        return wthaigd$border(0xFF808080, 1, isInnerBorder); // 默认参数
    }
}
