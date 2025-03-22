package com.pinkyudeer.wthaigd.gui.screen;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.gui.KeyBindGuiHandler;
import com.pinkyudeer.wthaigd.gui.widgetsDis.GuiButtonAdapter;
import com.pinkyudeer.wthaigd.gui.widgetsDis.GuiCheckBox;
import com.pinkyudeer.wthaigd.gui.widgetsDis.GuiIcon;
import com.pinkyudeer.wthaigd.gui.widgetsDis.GuiLabel;
import com.pinkyudeer.wthaigd.gui.widgetsDis.GuiTaskButton;
import com.pinkyudeer.wthaigd.gui.widgetsDis.GuiTextField;
import com.pinkyudeer.wthaigd.gui.widgetsDis.container.GuiContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMainWindowDis extends GuiScreen {

    private final GuiScreen parentScreen;
    private GuiButton returnButton;
    private boolean opened = false;

    // 主要容器
    private GuiContainer mainContainer;
    private GuiContainer leftPanel;
    private GuiContainer rightPanel;

    // 控件
    private GuiCheckBox testCheckBox;
    private GuiTextField testTextField;
    private GuiLabel testLabel;
    private GuiIcon testIcon;

    // 新增带滚动条的标签控件
    private GuiLabel scrollableLabel;
    private String longText = "这是一个长文本示例，用于展示标签的滚动功能。\n\n"
        + "自动换行能力：当文本太长时，标签会自动将内容换行显示，确保文本不会超出指定的宽度。这使得长段落文本能够正确展示。\n\n"
        + "垂直滚动条：当内容高度超过指定的最大高度时，会自动显示垂直滚动条。用户可以通过滚动条或鼠标滚轮来查看更多内容。\n\n"
        + "水平滚动条：对于宽度过大的文本，会显示水平滚动条，允许用户查看完整内容。\n\n"
        + "可自定义的外观：滚动条的大小、颜色都可以根据需要进行定制，以匹配界面的整体风格。\n\n"
        + "智能显示：只有当内容超过指定阈值时才会显示滚动条，避免不必要的界面元素，保持界面简洁。\n\n"
        + "这些特性使得GuiLabel特别适合显示帮助文本、说明文档、聊天记录等需要大量文字的场景。\n\n"
        + "请尝试使用鼠标滚轮或拖动滚动条来查看更多内容！";

    public GuiMainWindowDis(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        // 创建主容器，占据整个屏幕除了底部的区域
        mainContainer = new GuiContainer(0, 40, width, height - 100);
        mainContainer.setDebugPath("main");

        // 左侧面板 - 用于放置按钮
        leftPanel = new GuiContainer(0, 0, width / 4 * 3, height - 100);
        leftPanel.setDebugPath("main/leftPanel");
        leftPanel.enableVerticalScroll();
        leftPanel.setGridLayout(1, 15, 30);

        // 右侧面板 - 用于放置文本和其他控件
        rightPanel = new GuiContainer(260, 0, width / 4, height - 100);
        rightPanel.setDebugPath("main/rightPanel");

        // 添加两个面板到主容器
        mainContainer.addComponent(leftPanel);
        mainContainer.addComponent(rightPanel);

        // 向左侧面板添加按钮
        for (int i = 0; i < 15; i++) {
            leftPanel.addChild(new GuiTaskButton(i, 0, 0, 50, 20, "按钮 " + (i + 1)));
        }

        // 创建复选框并添加到按钮列表和右侧面板
        testCheckBox = new GuiCheckBox(20, 0, 10, "启用滚动条", false);
        buttonList.add(testCheckBox);
        // 添加复选框的适配器到右侧面板
        rightPanel.addComponent(GuiButtonAdapter.adapt(testCheckBox));

        // 创建标题标签
        testLabel = new GuiLabel(fontRendererObj, 0, 60, "这是一个高级UI演示");
        testLabel.setColor(0x55FFFF);
        testLabel.setShadow(true);
        rightPanel.addComponent(testLabel);

        // 创建文本框
        testTextField = new GuiTextField(fontRendererObj, 0, 90, 300, 20);
        testTextField.setText("输入文本...");
        testTextField.setFocused(false);
        rightPanel.addComponent(testTextField);

        // 创建图标
        testIcon = new GuiIcon(new ResourceLocation("wthaigd", "textures/items/hand_viewer.png"), 280, 10, 16, 16);
        testIcon.enableHoverEffect(0x80FFFFFF);
        testIcon.setClickHandler(() -> {
            Wthaigd.LOG.info("图标被点击了！");
            testCheckBox.toggle();
            updateScrollSettings();
        });
        rightPanel.addComponent(testIcon);

        // 创建带滚动条的标签
        scrollableLabel = new GuiLabel(fontRendererObj, 0, 130, longText, 0xFFFFFF);
        scrollableLabel.enableWordWrap(300);
        scrollableLabel.enableVerticalScroll(150);
        scrollableLabel.setScrollBarAppearance(8, 0xAAFFFFFF, 0x55000000);
        rightPanel.addComponent(scrollableLabel);

        // 添加底部返回按钮
        returnButton = addButton(new GuiTaskButton(100, width / 2 - 100, height - 40, 200, 20, "返回"));
    }

    /**
     * 更新滚动设置
     */
    private void updateScrollSettings() {
        if (testCheckBox.isChecked()) {
            leftPanel.enableVerticalScroll();
            scrollableLabel.enableVerticalScroll(500);
            scrollableLabel.enableHorizontalScroll(200);
        } else {
            leftPanel.disableVerticalScroll();
            scrollableLabel.disableVerticalScroll();
            scrollableLabel.disableHorizontalScroll();
            scrollableLabel.enableWordWrap(300);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == returnButton) {
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button == testCheckBox) {
            Wthaigd.LOG.info("复选框状态: " + ((GuiCheckBox) button).isChecked());
            updateScrollSettings();
        } else {
            // 处理容器中按钮的点击
            Wthaigd.LOG.info("Button " + button.id + " pressed!");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 绘制标题
        drawCenteredString(fontRendererObj, "UI控件示例", width / 2, 20, 0xFFFFFF);

        // 绘制主容器及其所有子控件
        mainContainer.render(mc, mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // 处理主容器内控件的点击
        mainContainer.mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);

        // 处理主容器的鼠标释放事件
        mainContainer.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        // 处理主容器的鼠标滚轮事件
        mainContainer.handleMouseInput(mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // 更新文本框的光标闪烁
        testTextField.updateCursorCounter();

        // 处理主容器的鼠标拖动
        int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
        mainContainer.mouseDragged(mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == org.lwjgl.input.Keyboard.KEY_F3) {
            // 切换UI调试模式
            GuiContainer.uiDebugMode = !GuiContainer.uiDebugMode;
            Wthaigd.LOG.info("UI Debug Mode: {}", GuiContainer.uiDebugMode);
        } else if (keyCode == KeyBindGuiHandler.openTaskGui.getKeyCode()) {
            if (opened) {
                this.mc.displayGuiScreen(null);
                opened = false;
            } else {
                opened = true;
            }
        } else if (testTextField.isFocused()) {
            // 处理文本框的键盘输入
            testTextField.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private <T extends GuiButton> T addButton(T button) {
        buttonList.add(button);
        return button;
    }
}
