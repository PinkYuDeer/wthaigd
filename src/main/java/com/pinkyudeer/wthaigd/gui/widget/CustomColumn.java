package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.pinkyudeer.wthaigd.helper.render.GLShaderDrawHelper;

public class CustomColumn extends Column implements ICustomAble<CustomColumn> {

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

    public CustomColumn custom(GLShaderDrawHelper.CustomRectConfig config) {
        rectConfig = config;
        return this;
    }
}
