package com.pinkyudeer.wthaigd.gui.screen.panel;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;

public class MainPanel extends ModularPanel {

    public MainPanel(int panelWidth, int panelHeight) {
        super("main");
        this.size(panelWidth, panelHeight);
        this.getArea()
            .setSize(panelWidth, panelHeight);
        this.background(IDrawable.EMPTY);

        // addChild(
        // applyBorder(mainLayout()).width(8)
        // .round()
        // .roundRadius(5)
        // .withBackground(0x00000060)
        // .color(0x99ccffbb)
        // .done(),
        // 0);
    }

    private Row mainLayout() {
        // 创建一个主布局，使用Row进行水平分块
        Row mainLayout = (Row) new Row().center()
            .background(IDrawable.EMPTY);

        float sidebarWidthRef = 0.16f; // 左侧侧边栏宽度

        // mainLayout.addChild(
        // applyBorder(sidebar(sidebarWidthRef)).select(MUI2Helper.RenderBorderEnum.RIGHT)
        // .width(4)
        // .color(0x99ccffbb)
        // .done(),
        // 0);
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
        // sidebar.addChild(
        // applyBorder(leftPanel(1 - infoRel)).select(MUI2Helper.RenderBorderEnum.TOP)
        // .color(0x99ccffbb)
        // .width(4)
        // .done(),
        // 1);

        return sidebar;
    }

    private Column leftPanel(float heightRef) {
        // 创建内容区域
        Column leftPanel = (Column) new Column().heightRel(heightRef)
            .background(IDrawable.EMPTY);

        return leftPanel;
    }

    private Column rightPanel(float widthRef) {
        // 创建右侧面板
        Column rightPanel = (Column) new Column().widthRel(widthRef)
            .background(IDrawable.EMPTY);

        float navBarHeightRef = 0.1f; // 导航条高度

        // rightPanel.addChild(
        // applyBorder(navBar(navBarHeightRef, widthRef)).select(MUI2Helper.RenderBorderEnum.BOTTOM)
        // .color(0x99ccffbb)
        // .width(4)
        // .done(),
        // 0);
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
            ButtonWidget<?> pageButton = new ButtonWidget<>().widthRel(1f / pageNum)
                .overlay(
                    IKey.str("页面" + i)
                        .color(0xdba1c7bb)
                        .shadow(false))
                .background(IDrawable.EMPTY);

            // pageSwitch.addChild(
            // applyBorder(pageButton).select(MUI2Helper.RenderBorderEnum.RIGHT)
            // .width(4)
            // .done(),
            // i);
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
