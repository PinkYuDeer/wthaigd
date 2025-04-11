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
import com.pinkyudeer.wthaigd.gui.widget.ICustomAble;
import com.pinkyudeer.wthaigd.helper.render.GLShaderDrawHelper;

@Mixin(Widget.class)
public abstract class WidgetMixin<W extends Widget<W>> implements ICustomAble<W> {

    @Unique
    private GLShaderDrawHelper.CustomRectConfig wthaigd$customRectConfig;
    @Unique
    private boolean wthaigd$configNeedSetup = true;

    @Shadow(remap = false)
    public abstract Area getArea();

    @Inject(method = "draw", at = @At("HEAD"), remap = false)
    public void wthaigd$draw(ModularGuiContext context, WidgetTheme widgetTheme, CallbackInfo ci) {
        if (wthaigd$customRectConfig != null) {
            if (wthaigd$configNeedSetup) {
                wthaigd$customRectConfig = wthaigd$customRectConfig.setup(getArea().w(), getArea().h());
                wthaigd$configNeedSetup = false;
            }
            GLShaderDrawHelper.drawComplexRect(wthaigd$customRectConfig);
        }
    }

    @Unique
    @SuppressWarnings({ "unchecked", "DataFlowIssue" })
    public W wthaigd$custom(GLShaderDrawHelper.CustomRectConfig config) {
        wthaigd$customRectConfig = config;
        return (W) (Object) this;
    }
}
