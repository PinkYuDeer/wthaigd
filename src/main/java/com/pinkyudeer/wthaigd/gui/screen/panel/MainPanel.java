package com.pinkyudeer.wthaigd.gui.screen.panel;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.pinkyudeer.wthaigd.helper.render.MUIHelper;

public class MainPanel extends ModularPanel {

    public MainPanel(int panelWidth, int panelHeight) {
        super("main");
        this.size(panelWidth, panelHeight);
        this.getArea()
            .setSize(panelWidth, panelHeight);
        this.background(IDrawable.EMPTY);
        addChild(
            MUIHelper.custom(mainLayout())
                .rectEdgeSoftness(1.0f)
                .border(1f, 0.6f, 0.0f, 0x99ccffff)
                .round(30)
                .rectColor(0x00000060)
                .done(),
            0);
    }

    private Row mainLayout() {
        // 创建一个主布局，使用Row进行水平分块
        Row mainLayout = (Row) new Row().center()
            .background(IDrawable.EMPTY);

        float sidebarWidthRef = 0.16f; // 左侧侧边栏宽度
        mainLayout.addChild(
            MUIHelper.custom(sidebar(sidebarWidthRef))
                .border(1f, 0.5f, 0x99ccffff)
                .borderSelect(false, false, false, true, false)
                .done(),
            0);
        mainLayout.addChild(rightPanel(1 - sidebarWidthRef), 1);

        return mainLayout;
    }

    private Column sidebar(float widthRef) {
        // 创建左侧面板
        Column sidebar = (Column) new Column().widthRel(widthRef)
            .background(IDrawable.EMPTY);

        float infoRel = 0.1f; // 信息区域高度

        IWidget mainText = IKey.str("wthaigd")
            .color(0xbb99ccff)
            .shadow(true)
            .asWidget()
            .heightRel(infoRel)
            .widthRel(1);

        sidebar.addChild(mainText, 0);
        sidebar.addChild(leftPanel(1 - infoRel), 1);

        return sidebar;
    }

    private Column leftPanel(float heightRef) {
        // 创建内容区域
        Column leftPanel = (Column) new Column().heightRel(heightRef)
            .background(IDrawable.EMPTY);

        leftPanel.addChild(
            MUIHelper.custom(
                new Row().height(1)
                    .widthRel(0.8f))
                .border(1f, -0.5f, 0x99ccffff)
                .borderSelect(true, false, false, false, false)
                .done(),
            0);
        return leftPanel;
    }

    private Column rightPanel(float widthRef) {
        // 创建右侧面板
        Column rightPanel = (Column) new Column().widthRel(widthRef)
            .background(IDrawable.EMPTY);

        float navBarHeightRef = 0.1f; // 导航条高度

        rightPanel.addChild(
            MUIHelper.custom(navBar(navBarHeightRef, widthRef))
                .border(1f, -0.5f, 0x99ccffff)
                .borderSelect(false, true, false, false, true)
                .done(),
            0);
        rightPanel.addChild(contentArea(1 - navBarHeightRef), 1);

        return rightPanel;
    }

    private Row navBar(float heightRef, float rightPanelWidthRef) {
        // 创建导航条
        Row navBar = (Row) new Row().heightRel(heightRef)
            .background(IDrawable.EMPTY);

        float pageSwitchRef = 0.8f; // 页面切换按钮区域总宽度

        navBar.addChild(pageSwitch(pageSwitchRef), 0);
        navBar.addChild(settingButtons(1 - pageSwitchRef, heightRef, rightPanelWidthRef), 1);
        return navBar;
    }

    private Row pageSwitch(float widthRef) {
        // 创建页面切换按钮
        Row pageSwitch = (Row) new Row().widthRel(widthRef)
            .background(IDrawable.EMPTY);

        int pageNum = 3; // 页面数量

        for (int i = 0; i < pageNum; i++) {

            ButtonWidget<?> pageButton = new ButtonWidget<>().widthRel(0.5f)
                .heightRel(0.8f)
                .overlay(
                    IKey.str("页面" + i)
                        .color(0xbb99ccff)
                        .shadow(false))
                .background(IDrawable.EMPTY);

            Row buttonDiv = (Row) new Row().widthRel(1f / pageNum)
                .heightRel(0.8f)
                .paddingLeft(25);
            buttonDiv.addChild(
                MUIHelper.custom(pageButton)
                    .round(10f)
                    .innerShadow(0.2f, -0.02f, 0.08f, 0xFFB1D9E0)
                    .shadow(0.05f, 0.02f, -0.08f, 0x000000ff)
                    .rectEdgeSoftness(1f)
                    .rectColor(0xFFB1D9A0)
                    .done(),
                0);

            pageSwitch.addChild(
                MUIHelper.custom(buttonDiv)
                    .border(1f, -0.5f, 0x99ccffff)
                    .borderSelect(false, false, false, true, false)
                    .done(),
                i);
        }

        return pageSwitch;
    }

    private Row settingButtons(float widthRef, float navBarHeightRef, float rightPanelWidthRef) {
        // 创建设置按钮区域
        Row settingButtons = (Row) new Row().widthRel(widthRef)
            .background(IDrawable.EMPTY);

        String[] buttonLabels = { "setting", "help", "add" };
        int buttonWidth = (int) (this.getArea()
            .h() * navBarHeightRef
            * 0.8f);
        int totalWidth = (int) (this.getArea()
            .w() * rightPanelWidthRef
            * widthRef);
        int margin = buttonWidth / 4;
        int padding = (totalWidth - buttonWidth * buttonLabels.length) / (buttonLabels.length + 1);
        for (int i = 0; i < buttonLabels.length; i++) {
            ButtonWidget<?> settingButton = new ButtonWidget<>().right((buttonWidth + padding) * i + margin)
                .alignY(0.5f)
                .size(buttonWidth)
                .background(UITexture.fullImage("wthaigd", "textures/gui/" + buttonLabels[i] + ".png"));

            settingButtons.addChild(settingButton, 0);
        }

        return settingButtons;
    }

    private Column contentArea(float heightRef) {
        // 创建内容区域
        Column contentArea = (Column) new Column().heightRel(heightRef)
            .background(IDrawable.EMPTY);

        return contentArea;
    }
}
