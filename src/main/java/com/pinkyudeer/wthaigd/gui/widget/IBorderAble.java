package com.pinkyudeer.wthaigd.gui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;

public interface IBorderAble<W extends IWidget> {

    W wthaigd$border(int wthaigd$borderColor, int wthaigd$borderSize, boolean isInnerBorder);

    W wthaigd$border(boolean isInnerBorder);

    W wthaigd$border();
}
