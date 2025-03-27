package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;

public interface IBorderAble<W extends IWidget> {

    /**
     * 设置圆角边框，可控制边框位置和圆角位置
     */
    W wthaigd$border(int wthaigd$borderColor, int wthaigd$borderSize, int wthaigd$borderRadius, int borderFlags,
        int cornerFlags);

    /**
     * 设置圆角背景填充，使用已有的圆角参数
     */
    W wthaigd$fillBackground(int backgroundColor);
}
