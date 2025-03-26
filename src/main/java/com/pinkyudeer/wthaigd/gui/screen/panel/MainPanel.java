package com.pinkyudeer.wthaigd.gui.screen.panel;

import static com.pinkyudeer.wthaigd.helper.render.RenderHelper.applyBorder;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.pinkyudeer.wthaigd.helper.render.RenderBorderEnum;

public class MainPanel extends ModularPanel {

    private final int panelWidth;
    private final int panelHeight;
    private final int sidebarWidth;
    private final int navBarHeight;

    public MainPanel(int panelWidth, int panelHeight) {
        super("main");
        this.size(panelWidth, panelHeight);
        this.background(IDrawable.EMPTY);
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.sidebarWidth = panelWidth / 6; // 侧边栏宽度
        this.navBarHeight = 20; // 导航条高度

        addChild(
            applyBorder(createMainLayout()).width(3)
                .round()
                .roundRadius(5)
                .done(),
            0);
        addChild(
            applyBorder(createMainLayout()).width(3)
                .round()
                .roundRadius(5)
                .done(),
            0);
    }

    private Row createMainLayout() {
        // 创建一个主布局，使用Row进行水平分块
        Row mainLayout = (Row) new Row().size(panelWidth, panelHeight)
            .center()
            .background(IDrawable.EMPTY);

        mainLayout.addChild(
            applyBorder(createSidebar()).select(RenderBorderEnum.RIGHT)
                .done(),
            0);
        mainLayout.addChild(createContentPanel(), 1);

        return mainLayout;
    }

    private Column createSidebar() {
        // 创建左侧面板
        Column sidebar = (Column) new Column().size(sidebarWidth, panelHeight)
            .background(IDrawable.EMPTY);

        ButtonWidget<?> sidebarButton = new ButtonWidget<>().size(65, navBarHeight)
            .overlay(IKey.str("测试"))
            .background(IDrawable.EMPTY);

        sidebar.addChild(
            applyBorder(sidebarButton).round()
                .roundRadius(3)
                .color(0x000000)
                .done(),
            0);

        return sidebar;
    }

    private Column createContentPanel() {
        // 创建右侧面板
        Column content = (Column) new Column().size(panelWidth - sidebarWidth, panelHeight)
            .background(IDrawable.EMPTY);

        content.addChild(
            applyBorder(createNavBar()).select(RenderBorderEnum.BOTTOM)
                .done(),
            0);
        content.addChild(createContentArea(), 1);

        return content;
    }

    private Row createNavBar() {
        // 创建导航条
        Row navBar = (Row) new Row().size(panelWidth - sidebarWidth, navBarHeight)
            .background(IDrawable.EMPTY)
            .childPadding(10);

        ButtonWidget<?> navButton1 = new ButtonWidget<>().size(30, navBarHeight)
            .overlay(IKey.str("测试1"))
            .background(IDrawable.EMPTY);

        ButtonWidget<?> navButton2 = new ButtonWidget<>().size(30, navBarHeight)
            .overlay(IKey.str("测试2"))
            .background(IDrawable.EMPTY);

        ButtonWidget<?> navButton3 = new ButtonWidget<>().size(30, navBarHeight)
            .overlay(IKey.str("测试3"))
            .background(IDrawable.EMPTY);

        navBar.addChild(
            applyBorder(navButton1).round()
                .roundRadius(3)
                .done(),
            0);
        navBar.addChild(
            applyBorder(navButton2).round()
                .roundRadius(3)
                .done(),
            1);
        navBar.addChild(
            applyBorder(navButton3).round()
                .roundRadius(3)
                .done(),
            2);

        return navBar;
    }

    private Column createContentArea() {
        // 创建内容区域
        Column contentArea = (Column) new Column().size(panelWidth - sidebarWidth, panelHeight - navBarHeight)
            .background(IDrawable.EMPTY);

        contentArea.addChild(
            new RichTextWidget().size(panelWidth - sidebarWidth, panelHeight - navBarHeight)
                .add("test"),
            0);

        return contentArea;
    }
}
