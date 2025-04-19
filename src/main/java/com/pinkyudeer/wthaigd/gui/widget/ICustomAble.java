package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.pinkyudeer.wthaigd.helper.render.GLShaderDrawHelper;

public interface ICustomAble<W extends IWidget> {

    W custom(GLShaderDrawHelper.CustomRectConfig config);
}
