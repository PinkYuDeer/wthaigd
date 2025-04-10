package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.pinkyudeer.wthaigd.helper.render.GLShaderDrawHelper;

public interface ICustomAble<W extends IWidget> {

    /**
     * 设置自定义项目
     */
    W wthaigd$custom(GLShaderDrawHelper.CustomRectConfig config);
}
