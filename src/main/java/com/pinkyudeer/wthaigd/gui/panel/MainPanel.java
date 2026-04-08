package com.pinkyudeer.wthaigd.gui.panel;

import java.util.List;
import java.util.UUID;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.pinkyudeer.wthaigd.gui.drawable.ShaderDrawable;
import com.pinkyudeer.wthaigd.gui.screen.TaskScreen;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.service.TaskService;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MainPanel extends ModularPanel {

    private static final int ACCENT = 0x99ccffff;
    private static final int PANEL_BG = 0x181820C0;
    private static final int SIDEBAR_BG = 0x141828B0;
    private static final int ITEM_BG = 0x20203040;
    private static final int ITEM_HOVER = 0x30406060;
    private static final int BTN_BG = 0x335588a0;
    private static final int BTN_HOVER = 0x4477aacc;

    private final TaskScreen taskScreen;

    public MainPanel(TaskScreen taskScreen) {
        super("main");
        this.taskScreen = taskScreen;
        sizeRel(0.88f, 0.84f);
        center();
        background(IDrawable.EMPTY);
        overlay(ShaderDrawable.panel(12f, PANEL_BG, ACCENT));

        child(buildLayout());
    }

    @Override
    public void closeIfOpen() {
        if (!isOpen()) return;
        if (taskScreen.isClosing()) {
            super.closeIfOpen();
            return;
        }
        taskScreen.startClosing(super::closeIfOpen);
    }

    private Row buildLayout() {
        Row layout = new Row();
        layout.widthRel(1f)
            .heightRel(1f)
            .padding(6);
        layout.child(buildSidebar());
        layout.child(buildContentArea());
        return layout;
    }

    private Column buildSidebar() {
        Column sidebar = new Column();
        sidebar.widthRel(0.18f)
            .heightRel(1f);
        sidebar.background(ShaderDrawable.roundedRect(8f, SIDEBAR_BG));
        sidebar.padding(8);

        sidebar.child(
            IKey.str("WTHAIGD")
                .color(ACCENT)
                .shadow(true)
                .asWidget()
                .heightRel(0.08f)
                .widthRel(1f));

        sidebar.child(navButton("Tasks", true));
        sidebar.child(navButton("Teams", false));
        sidebar.child(navButton("Tags", false));

        return sidebar;
    }

    private ButtonWidget<?> navButton(String label, boolean active) {
        int bg = active ? BTN_BG : 0x00000000;
        return new ButtonWidget<>().widthRel(0.9f)
            .height(22)
            .marginTop(4)
            .background(ShaderDrawable.roundedRect(6f, bg))
            .overlay(
                IKey.str(label)
                    .color(active ? 0xFFFFFFff : 0xAAAAAAff)
                    .shadow(active));
    }

    private Column buildContentArea() {
        Column content = new Column();
        content.widthRel(0.82f)
            .heightRel(1f);
        content.paddingLeft(8);

        content.child(buildHeader());
        content.child(buildTaskList());

        return content;
    }

    private Row buildHeader() {
        Row header = new Row();
        header.widthRel(1f)
            .height(32);
        header.marginBottom(6);

        header.child(
            IKey.str("Active Tasks")
                .color(0xEEEEEEff)
                .shadow(true)
                .asWidget()
                .widthRel(0.7f)
                .heightRel(1f));

        header.child(
            new ButtonWidget<>().widthRel(0.25f)
                .height(24)
                .alignY(0.5f)
                .background(ShaderDrawable.roundedRect(6f, BTN_BG))
                .hoverBackground(ShaderDrawable.roundedRect(6f, BTN_HOVER))
                .overlay(
                    IKey.str("+ New Task")
                        .color(0xFFFFFFff)
                        .shadow(true))
                .onMousePressed(btn -> {
                    TaskFormPanel.openCreate(getScreen());
                    return true;
                }));

        return header;
    }

    private Column buildTaskList() {
        Column list = new Column();
        list.widthRel(1f)
            .heightRel(0.85f);

        List<Task> tasks = TaskService.getActiveTasks();
        if (tasks.isEmpty()) {
            list.child(
                IKey.str("No tasks yet. Click '+ New Task' to create one.")
                    .color(0x888888ff)
                    .asWidget()
                    .widthRel(1f)
                    .height(30)
                    .marginTop(20));
        } else {
            for (Task task : tasks) {
                list.child(buildTaskItem(task));
            }
        }

        return list;
    }

    private Row buildTaskItem(Task task) {
        int priorityColor = getPriorityColor(task.getPriority());
        boolean completed = task.getStatus() == Task.TaskStatus.Completed || task.getStatus() == Task.TaskStatus.Closed;

        Row item = new Row();
        item.widthRel(1f)
            .height(28);
        item.marginTop(2);
        item.background(ShaderDrawable.roundedRect(4f, ITEM_BG));
        item.hoverBackground(ShaderDrawable.roundedRect(4f, ITEM_HOVER));
        item.padding(4);

        String statusIcon = completed ? "✓" : "○";
        item.child(
            IKey.str(statusIcon)
                .color(completed ? 0x66CC66ff : 0xAAAAAAff)
                .asWidget()
                .width(16)
                .heightRel(1f));

        item.child(
            IKey.str(task.getTitle())
                .color(completed ? 0x888888ff : 0xDDDDDDff)
                .asWidget()
                .widthRel(0.55f)
                .heightRel(1f));

        String prioLabel = formatPriority(task.getPriority());
        item.child(
            IKey.str(prioLabel)
                .color(priorityColor)
                .asWidget()
                .widthRel(0.15f)
                .heightRel(1f));

        item.child(
            IKey.str(
                task.getStatus()
                    .name())
                .color(0x99999ff)
                .asWidget()
                .widthRel(0.15f)
                .heightRel(1f));

        if (!completed) {
            item.child(
                new ButtonWidget<>().size(20)
                    .alignY(0.5f)
                    .background(ShaderDrawable.roundedRect(4f, 0x336633c0))
                    .overlay(
                        IKey.str("✓")
                            .color(0x66CC66ff))
                    .onMousePressed(btn -> {
                        UUID operatorId = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getUniqueID();
                        TaskService.completeTask(task.getId(), operatorId);
                        getScreen().close();
                        return true;
                    }));
        }

        return item;
    }

    private int getPriorityColor(Task.Priority priority) {
        return switch (priority) {
            case CRITICAL -> 0xFF4444ff;
            case P1, P2 -> 0xFF8844ff;
            case P3, P4 -> 0xFFCC44ff;
            case P5, P6 -> 0x44AAFFff;
            case P7, P8, P9 -> 0x88CC88ff;
            case UNDEFINED -> 0x888888ff;
        };
    }

    private String formatPriority(Task.Priority priority) {
        return switch (priority) {
            case CRITICAL -> "CRIT";
            case UNDEFINED -> "---";
            default -> priority.name();
        };
    }
}
