package com.pinkyudeer.wthaigd.gui.panel;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
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

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private enum SortMode {
        PRIORITY,
        TIME_DESC,
        TIME_ASC
    }

    private final TaskScreen taskScreen;
    private ListWidget<IWidget, ?> taskListWidget;
    private IPanelHandler formHandler;
    private SortMode currentSort = SortMode.PRIORITY;
    private boolean showCompleted = false;
    private ButtonWidget<?> sortPrioBtn;
    private ButtonWidget<?> sortTimeBtn;
    private ButtonWidget<?> filterDoneBtn;

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
        content.child(buildSortBar());

        taskListWidget = new ListWidget<>();
        taskListWidget.widthRel(1f)
            .heightRel(0.82f);
        populateTaskList();
        content.child(taskListWidget);

        return content;
    }

    private Row buildHeader() {
        Row header = new Row();
        header.widthRel(1f)
            .height(32);
        header.marginBottom(2);

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
                    openTaskForm();
                    return true;
                }));

        return header;
    }

    private static final int SORT_ACTIVE = 0x3366aa99;
    private static final int SORT_INACTIVE = 0x1a223344;

    private Row buildSortBar() {
        Row bar = new Row();
        bar.widthRel(1f)
            .height(16)
            .marginBottom(4);

        bar.child(
            IKey.str("Sort:")
                .color(0x777777ff)
                .asWidget()
                .width(30)
                .heightRel(1f));

        sortPrioBtn = buildSortButton("Priority", SortMode.PRIORITY);
        sortTimeBtn = buildSortButton("Time", SortMode.TIME_DESC);
        bar.child(sortPrioBtn);
        bar.child(sortTimeBtn);

        bar.child(
            IKey.str("  ")
                .asWidget()
                .width(10)
                .heightRel(1f));

        filterDoneBtn = buildFilterDoneButton();
        bar.child(filterDoneBtn);

        return bar;
    }

    private static final int FILTER_ON = 0x336644aa;
    private static final int FILTER_OFF = 0x1a223344;

    private ButtonWidget<?> buildFilterDoneButton() {
        int bg = showCompleted ? FILTER_ON : FILTER_OFF;
        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.width(52)
            .height(14)
            .alignY(0.5f)
            .marginLeft(3)
            .background(ShaderDrawable.roundedRect(3f, bg))
            .hoverBackground(ShaderDrawable.roundedRect(3f, BTN_HOVER))
            .overlay(
                IKey.str(showCompleted ? "Done: ON" : "Done: OFF")
                    .color(showCompleted ? 0x88FF88ff : 0x777777ff)
                    .shadow(showCompleted)
                    .scale(0.85f))
            .onMousePressed(b -> {
                showCompleted = !showCompleted;
                refreshFilterButton();
                refreshTaskList();
                return true;
            });
        return btn;
    }

    private void refreshFilterButton() {
        if (filterDoneBtn == null) return;
        int bg = showCompleted ? FILTER_ON : FILTER_OFF;
        filterDoneBtn.background(ShaderDrawable.roundedRect(3f, bg))
            .overlay(
                IKey.str(showCompleted ? "Done: ON" : "Done: OFF")
                    .color(showCompleted ? 0x88FF88ff : 0x777777ff)
                    .shadow(showCompleted)
                    .scale(0.85f));
    }

    private ButtonWidget<?> buildSortButton(String label, SortMode mode) {
        boolean active = (currentSort == mode) || (mode == SortMode.TIME_DESC && currentSort == SortMode.TIME_ASC);
        String arrow = "";
        if (mode == SortMode.TIME_DESC && (currentSort == SortMode.TIME_DESC || currentSort == SortMode.TIME_ASC)) {
            arrow = currentSort == SortMode.TIME_ASC ? " ^" : " v";
        }
        int bg = active ? SORT_ACTIVE : SORT_INACTIVE;

        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.width(48)
            .height(14)
            .alignY(0.5f)
            .marginLeft(3)
            .background(ShaderDrawable.roundedRect(3f, bg))
            .hoverBackground(ShaderDrawable.roundedRect(3f, BTN_HOVER))
            .overlay(
                IKey.str(label + arrow)
                    .color(active ? 0xFFFFFFff : 0x999999ff)
                    .shadow(active)
                    .scale(0.85f))
            .onMousePressed(b -> {
                if (mode == SortMode.PRIORITY) {
                    currentSort = SortMode.PRIORITY;
                } else {
                    currentSort = (currentSort == SortMode.TIME_DESC) ? SortMode.TIME_ASC : SortMode.TIME_DESC;
                }
                refreshTaskList();
                refreshSortButtons();
                return true;
            });
        return btn;
    }

    private void refreshSortButtons() {
        if (sortPrioBtn == null || sortTimeBtn == null) return;
        boolean prioActive = currentSort == SortMode.PRIORITY;
        boolean timeActive = currentSort == SortMode.TIME_DESC || currentSort == SortMode.TIME_ASC;
        String timeArrow = currentSort == SortMode.TIME_ASC ? " ^" : (timeActive ? " v" : "");

        sortPrioBtn.background(ShaderDrawable.roundedRect(3f, prioActive ? SORT_ACTIVE : SORT_INACTIVE))
            .overlay(
                IKey.str("Priority")
                    .color(prioActive ? 0xFFFFFFff : 0x999999ff)
                    .shadow(prioActive)
                    .scale(0.85f));
        sortTimeBtn.background(ShaderDrawable.roundedRect(3f, timeActive ? SORT_ACTIVE : SORT_INACTIVE))
            .overlay(
                IKey.str("Time" + timeArrow)
                    .color(timeActive ? 0xFFFFFFff : 0x999999ff)
                    .shadow(timeActive)
                    .scale(0.85f));
    }

    private void openTaskForm() {
        if (formHandler == null) {
            formHandler = IPanelHandler.simple(
                this,
                (ModularPanel parentPanel, EntityPlayer player) -> new TaskFormPanel(this::refreshTaskList),
                true);
        }
        if (!formHandler.isPanelOpen()) {
            formHandler.openPanel();
        }
    }

    private void openTaskDetail(Task task) {
        IPanelHandler handler = IPanelHandler.simple(
            this,
            (ModularPanel parentPanel, EntityPlayer player) -> new TaskDetailPanel(task, this::refreshTaskList),
            true);
        handler.openPanel();
    }

    private void populateTaskList() {
        try {
            List<Task> tasks = showCompleted ? TaskService.getAllTasks() : TaskService.getActiveTasks();
            tasks.removeIf(t -> t.getParentTaskId() != null);
            if (tasks.isEmpty()) {
                taskListWidget.child(
                    IKey.str("No tasks yet. Click '+ New Task' to create one.")
                        .color(0x888888ff)
                        .asWidget()
                        .widthRel(1f)
                        .height(30)
                        .marginTop(20));
            } else {
                sortTasks(tasks);
                for (Task task : tasks) {
                    taskListWidget.child(buildTaskItem(task));
                }
            }
        } catch (Exception e) {
            taskListWidget.child(
                IKey.str("Failed to load tasks.")
                    .color(0xFF4444ff)
                    .asWidget()
                    .widthRel(1f)
                    .height(30)
                    .marginTop(20));
        }
    }

    private void sortTasks(List<Task> tasks) {
        Comparator<Task> cmp = switch (currentSort) {
            case TIME_ASC -> Comparator.comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()));
            case TIME_DESC -> Comparator
                .comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing(
                t -> t.getPriority()
                    .ordinal());
        };
        tasks.sort(cmp);
    }

    public void refreshTaskList() {
        if (taskListWidget == null) return;
        taskListWidget.removeAll();
        populateTaskList();
        taskListWidget.scheduleResize();
    }

    private ButtonWidget<?> buildTaskItem(Task task) {
        boolean completed = task.getStatus() == Task.TaskStatus.Completed || task.getStatus() == Task.TaskStatus.Closed;

        String prioLabel = formatPriority(task.getPriority());
        int prioColor = getPriorityColor(task.getPriority());
        String statusLabel = task.getStatus()
            .name();
        int statusColor = getStatusColor(task.getStatus());
        int subCount = TaskService.getSubtaskCount(task.getId());
        String subLabel = subCount > 0 ? " (" + subCount + ")" : "";

        IDrawable itemOverlay = (context, x, y, width, height, widgetTheme) -> {
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);

            int textY = y + (height - fr.FONT_HEIGHT) / 2;
            int pad = 6;

            int cx = x + pad;
            String icon = completed ? "[x] " : "[ ] ";
            fr.drawStringWithShadow(icon, cx, textY, completed ? 0x66CC66 : 0xAAAAAA);
            cx += fr.getStringWidth(icon);

            String rightPart = statusLabel + subLabel;
            int rightW = fr.getStringWidth(rightPart);
            int maxTitleW = width - pad * 2
                - fr.getStringWidth(icon)
                - fr.getStringWidth(" [" + prioLabel + "]")
                - rightW
                - 20;

            String title = task.getTitle();
            if (maxTitleW > 0 && fr.getStringWidth(title) > maxTitleW) {
                while (fr.getStringWidth(title + "..") > maxTitleW && title.length() > 1) {
                    title = title.substring(0, title.length() - 1);
                }
                title += "..";
            }
            fr.drawStringWithShadow(title, cx, textY, completed ? 0x888888 : 0xDDDDDD);
            cx += fr.getStringWidth(title);

            fr.drawStringWithShadow(" [", cx, textY, 0x666666);
            cx += fr.getStringWidth(" [");
            fr.drawStringWithShadow(prioLabel, cx, textY, prioColor);
            cx += fr.getStringWidth(prioLabel);
            fr.drawStringWithShadow("]", cx, textY, 0x666666);

            int rx = x + width - pad;
            if (subCount > 0) {
                String subStr = "(" + subCount + ")";
                rx -= fr.getStringWidth(subStr);
                fr.drawStringWithShadow(subStr, rx, textY, 0x7799AA);
                rx -= 4;
            }
            rx -= fr.getStringWidth(statusLabel);
            fr.drawStringWithShadow(statusLabel, rx, textY, statusColor);

            GL11.glPopMatrix();
        };

        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.widthRel(1f)
            .height(26)
            .marginTop(2)
            .background(ShaderDrawable.roundedRect(4f, ITEM_BG))
            .hoverBackground(ShaderDrawable.roundedRect(4f, ITEM_HOVER))
            .overlay(itemOverlay)
            .onMousePressed(b -> {
                openTaskDetail(task);
                return true;
            });

        btn.tooltip(tip -> {
            tip.textShadow(true);
            tip.add(
                IKey.str(task.getTitle())
                    .color(0xFFFFFF))
                .newLine();
            tip.spaceLine(2);

            String desc = task.getDescription();
            if (desc != null && !desc.isEmpty()) {
                tip.add(
                    IKey.str(desc)
                        .color(0xBBBBBB))
                    .newLine();
                tip.spaceLine(2);
            }

            tip.add(
                IKey.str("Priority: ")
                    .color(0x999999))
                .add(
                    IKey.str(
                        task.getPriority()
                            .name())
                        .color(prioColor))
                .newLine();
            tip.add(
                IKey.str("Status:   ")
                    .color(0x999999))
                .add(
                    IKey.str(statusLabel)
                        .color(statusColor))
                .newLine();
            tip.add(
                IKey.str("Importance: ")
                    .color(0x999999))
                .add(
                    IKey.str(
                        task.getImportance()
                            .name())
                        .color(0xBBBBBB))
                .newLine();
            tip.add(
                IKey.str("Urgency:    ")
                    .color(0x999999))
                .add(
                    IKey.str(
                        task.getUrgency()
                            .name())
                        .color(0xBBBBBB))
                .newLine();

            if (subCount > 0) {
                tip.add(
                    IKey.str("Subtasks:   ")
                        .color(0x999999))
                    .add(
                        IKey.str(String.valueOf(subCount))
                            .color(0x7799AA))
                    .newLine();
            }

            if (task.getCreateTime() != null) {
                tip.spaceLine(2);
                tip.add(
                    IKey.str(
                        "Created: " + task.getCreateTime()
                            .format(TIME_FMT))
                        .color(0x777777))
                    .newLine();
            }
        });

        return btn;
    }

    private int getPriorityColor(Task.Priority priority) {
        return switch (priority) {
            case CRITICAL -> 0xFF4444;
            case P1, P2 -> 0xFF8844;
            case P3, P4 -> 0xFFCC44;
            case P5, P6 -> 0x44AAFF;
            case P7, P8, P9 -> 0x88CC88;
            case UNDEFINED -> 0x888888;
        };
    }

    private int getStatusColor(Task.TaskStatus status) {
        return switch (status) {
            case InProgress -> 0x44AAFF;
            case Completed -> 0x66CC66;
            case Closed -> 0x666666;
            case Canceled -> 0xCC4444;
            case Blocked -> 0xFF8844;
            case Rejected -> 0xCC4444;
            case Postponed -> 0xAAAA44;
            case Defect -> 0xFF6644;
            case InTrialRun -> 0x88CCAA;
            default -> 0xAAAAAA;
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
