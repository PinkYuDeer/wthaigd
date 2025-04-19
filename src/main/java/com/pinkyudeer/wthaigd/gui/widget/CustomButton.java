package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.pinkyudeer.wthaigd.helper.render.GLShaderDrawHelper;

public class CustomButton<W extends CustomButton<W>> extends ButtonWidget<W> implements ICustomAble<CustomButton<W>> {

    private GLShaderDrawHelper.CustomRectConfig rectConfig;
    private boolean configNotInit = true;

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (rectConfig != null) {
            if (configNotInit) {
                rectConfig = rectConfig.setup(getArea().w(), getArea().h());
                configNotInit = false;
            }
            GLShaderDrawHelper.drawComplexRect(rectConfig);
        }
        super.draw(context, widgetTheme);
    }

    public CustomButton<W> custom(GLShaderDrawHelper.CustomRectConfig config) {
        rectConfig = config;
        return this;
    }
}
