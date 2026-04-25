package com.pinkyudeer.wthaigd.gui.panel;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;
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
import com.pinkyudeer.wthaigd.client.TaskClientStore;
import com.pinkyudeer.wthaigd.gui.drawable.ShaderDrawable;
import com.pinkyudeer.wthaigd.gui.screen.TaskScreen;
import com.pinkyudeer.wthaigd.network.handler.NetMainSync;
import com.pinkyudeer.wthaigd.task.entity.Task;

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
    private static final int SORT_ACTIVE = 0x3366aa99;
    private static final int SORT_INACTIVE = 0x1a223344;
    private static final int FILTER_ON = 0x336644aa;
    private static final int FILTER_OFF = 0x1a223344;
    private static final int SUB_INDENT = 16;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private enum SortMode {
        PRIORITY,
        TIME_DESC,
        TIME_ASC
    }

    private final TaskScreen taskScreen;
    private ListWidget<IWidget, ?> taskListWidget;
    private IPanelHandler formHandler;
    private IPanelHandler detailHandler;
    private String lastDetailTaskId;
    private SortMode currentSort = SortMode.PRIORITY;
    private boolean showCompleted = false;
    private ButtonWidget<?> sortPrioBtn;
    private ButtonWidget<?> sortTimeBtn;
    private ButtonWidget<?> filterDoneBtn;
    private final Set<String> expandedTasks = new HashSet<>();

    private List<Task> cachedTasks = new ArrayList<>();
    private final Map<String, List<Task>> cachedSubtasks = new HashMap<>();
    private final Map<String, Integer> cachedSubCounts = new HashMap<>();

    public MainPanel(TaskScreen taskScreen) {
        super("wthaigd_main_panel");
        this.taskScreen = taskScreen;
        sizeRel(0.88f, 0.84f);
        center();
        background(IDrawable.EMPTY);
        overlay(ShaderDrawable.panel(12f, PANEL_BG, ACCENT));
        NetMainSync.requestSync();
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

    // --- Cache management ---

    private void reloadCache() {
        cachedSubtasks.clear();
        cachedSubCounts.clear();
        try {
            cachedTasks = TaskClientStore.INSTANCE.getTaskList(showCompleted);
        } catch (Exception e) {
            cachedTasks = new ArrayList<>();
        }
        for (Task t : cachedTasks) {
            if (t.getParentTaskId() != null) continue;
            ensureSubtasksCached(t.getId());
        }
    }

    private void ensureSubtasksCached(String taskId) {
        if (cachedSubCounts.containsKey(taskId)) return;
        List<Task> subs = TaskClientStore.INSTANCE.getSubtasks(taskId);
        cachedSubtasks.put(taskId, subs);
        cachedSubCounts.put(taskId, subs.size());
        if (expandedTasks.contains(taskId)) {
            for (Task sub : subs) {
                ensureSubtasksCached(sub.getId());
            }
        }
    }

    private int getCachedSubCount(String taskId) {
        if (!cachedSubCounts.containsKey(taskId)) {
            ensureSubtasksCached(taskId);
        }
        return cachedSubCounts.getOrDefault(taskId, 0);
    }

    private List<Task> getCachedSubtasks(String taskId) {
        if (!cachedSubCounts.containsKey(taskId)) {
            ensureSubtasksCached(taskId);
        }
        return cachedSubtasks.getOrDefault(taskId, new ArrayList<>());
    }

    // --- Layout ---

    private Row buildLayout() {
        Row layout = new Row();
        layout.widthRel(1f)
            .heightRel(1f)
            .padding(6)
            .name("main/layout");
        layout.child(buildSidebar());
        layout.child(buildContentArea());
        return layout;
    }

    private Column buildSidebar() {
        Column sidebar = new Column();
        sidebar.widthRel(0.18f)
            .heightRel(1f)
            .name("main/sidebar");
        sidebar.background(ShaderDrawable.roundedRect(8f, SIDEBAR_BG));
        sidebar.padding(8);
        sidebar.child(
            IKey.str("WTHAIGD")
                .color(ACCENT)
                .shadow(true)
                .asWidget()
                .heightRel(0.08f)
                .widthRel(1f)
                .name("sidebar/logo"));
        sidebar.child(navButton("Tasks", true, "sidebar/nav_tasks"));
        sidebar.child(navButton("Teams", false, "sidebar/nav_teams"));
        sidebar.child(navButton("Tags", false, "sidebar/nav_tags"));
        return sidebar;
    }

    private ButtonWidget<?> navButton(String label, boolean active, String widgetName) {
        int bg = active ? BTN_BG : 0x00000000;
        return new ButtonWidget<>().widthRel(0.9f)
            .height(22)
            .marginTop(4)
            .name(widgetName)
            .background(ShaderDrawable.roundedRect(6f, bg))
            .overlay(
                IKey.str(label)
                    .color(active ? 0xFFFFFFff : 0xAAAAAAff)
                    .shadow(active));
    }

    private Column buildContentArea() {
        Column content = new Column();
        content.widthRel(0.82f)
            .heightRel(1f)
            .name("main/content");
        content.paddingLeft(8);
        content.child(buildHeader());
        content.child(buildSortBar());

        taskListWidget = new ListWidget<>();
        taskListWidget.widthRel(1f)
            .heightRel(0.82f)
            .name("content/task_list");
        populateTaskList(true);
        content.child(taskListWidget);
        return content;
    }

    private Row buildHeader() {
        Row header = new Row();
        header.widthRel(1f)
            .height(32)
            .marginBottom(2)
            .name("content/header");
        header.child(
            IKey.str("Active Tasks")
                .color(0xEEEEEEff)
                .shadow(true)
                .asWidget()
                .widthRel(0.7f)
                .heightRel(1f)
                .name("header/title"));
        header.child(
            new ButtonWidget<>().widthRel(0.25f)
                .height(24)
                .alignY(0.5f)
                .name("header/btn_new_task")
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

    private Row buildSortBar() {
        Row bar = new Row();
        bar.widthRel(1f)
            .height(16)
            .marginBottom(4)
            .name("content/sort_bar");
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

    private ButtonWidget<?> buildFilterDoneButton() {
        int bg = showCompleted ? FILTER_ON : FILTER_OFF;
        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.width(52)
            .height(14)
            .alignY(0.5f)
            .marginLeft(3)
            .name("sort_bar/filter_done")
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
            .name("sort_bar/sort_" + label.toLowerCase())
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

    // --- Panel open ---

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

    private Task pendingDetailTask;

    private void openTaskDetail(Task task) {
        this.pendingDetailTask = task;
        if (detailHandler == null) {
            lastDetailTaskId = task.getId();
            detailHandler = IPanelHandler.simple(
                this,
                (ModularPanel parentPanel,
                    EntityPlayer player) -> new TaskDetailPanel(pendingDetailTask, this::refreshTaskList),
                true);
        } else {
            if (detailHandler.isPanelOpen()) return;
            if (!task.getId()
                .equals(lastDetailTaskId)) {
                detailHandler.deleteCachedPanel();
                lastDetailTaskId = task.getId();
            }
        }
        detailHandler.openPanel();
    }

    // --- Task list populate ---

    private void populateTaskList(boolean fromDb) {
        try {
            if (fromDb) {
                reloadCache();
            }
            List<Task> topLevel = new ArrayList<>();
            for (Task t : cachedTasks) {
                if (t.getParentTaskId() == null) topLevel.add(t);
            }
            if (topLevel.isEmpty()) {
                taskListWidget.child(
                    IKey.str("No tasks yet. Click '+ New Task' to create one.")
                        .color(0x888888ff)
                        .asWidget()
                        .widthRel(1f)
                        .height(30)
                        .marginTop(20)
                        .name("task_list/empty_hint"));
            } else {
                sortTasks(topLevel);
                for (Task task : topLevel) {
                    addTaskAndChildren(task, 0);
                }
            }
        } catch (Exception e) {
            taskListWidget.child(
                IKey.str("Failed to load tasks.")
                    .color(0xFF4444ff)
                    .asWidget()
                    .widthRel(1f)
                    .height(30)
                    .marginTop(20)
                    .name("task_list/error_hint"));
        }
    }

    private void addTaskAndChildren(Task task, int depth) {
        taskListWidget.child(buildTaskItem(task, depth));
        if (expandedTasks.contains(task.getId())) {
            List<Task> subs = getCachedSubtasks(task.getId());
            sortTasks(subs);
            for (Task sub : subs) {
                addTaskAndChildren(sub, depth + 1);
            }
        }
    }

    private void toggleExpand(Task task, boolean recursive) {
        String id = task.getId();
        if (expandedTasks.contains(id)) {
            if (recursive) {
                collapseRecursive(id);
            } else {
                expandedTasks.remove(id);
            }
        } else {
            expandedTasks.add(id);
            if (recursive) {
                expandRecursive(id);
            }
        }
        rebuildFromCache();
    }

    private void expandRecursive(String taskId) {
        expandedTasks.add(taskId);
        for (Task sub : getCachedSubtasks(taskId)) {
            expandRecursive(sub.getId());
        }
    }

    private void collapseRecursive(String taskId) {
        expandedTasks.remove(taskId);
        for (Task sub : getCachedSubtasks(taskId)) {
            collapseRecursive(sub.getId());
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

    /** Rebuild UI from cached data only, no DB queries. */
    private void rebuildFromCache() {
        if (taskListWidget == null) return;
        taskListWidget.removeAll();
        populateTaskList(false);
        taskListWidget.scheduleResize();
    }

    /** Reload from DB and rebuild the UI. Called when data changes. */
    public void refreshTaskList() {
        if (taskListWidget == null) return;
        if (detailHandler != null) {
            detailHandler.deleteCachedPanel();
            lastDetailTaskId = null;
        }
        taskListWidget.removeAll();
        populateTaskList(true);
        taskListWidget.scheduleResize();
    }

    // --- Status icon mapping ---

    private static String getStatusIcon(Task.TaskStatus status) {
        return switch (status) {
            case Completed -> "[/] ";
            case Closed -> "[-] ";
            case InProgress -> "[~] ";
            case Blocked -> "[!] ";
            case Postponed -> "[>] ";
            case Canceled -> "[x] ";
            case Rejected -> "[x] ";
            case InTrialRun -> "[?] ";
            default -> "[ ] ";
        };
    }

    private static int getStatusIconColor(Task.TaskStatus status) {
        return switch (status) {
            case Completed -> 0x66CC66;
            case Closed -> 0x666666;
            case InProgress -> 0x44AAFF;
            case Blocked -> 0xFF8844;
            case Postponed -> 0xAAAA44;
            case Canceled, Rejected -> 0xCC4444;
            case InTrialRun -> 0x88CCAA;
            default -> 0xAAAAAA;
        };
    }

    // --- Task item builder ---

    private ButtonWidget<?> buildTaskItem(Task task, int depth) {
        Task.TaskStatus status = task.getStatus();
        String prioLabel = formatPriority(task.getPriority());
        int prioColor = getPriorityColor(task.getPriority());
        String statusLabel = status.name();
        int statusColor = getStatusColor(status);
        int subCount = getCachedSubCount(task.getId());
        boolean expanded = expandedTasks.contains(task.getId());
        int indent = depth * SUB_INDENT;
        String statusIcon = getStatusIcon(status);
        int iconColor = getStatusIconColor(status);

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int arrowWidth = subCount > 0 ? fr.getStringWidth(expanded ? "v " : "> ") : 0;

        IDrawable itemOverlay = (context, x, y, width, height, widgetTheme) -> {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);

            int textY = y + (height - fr.FONT_HEIGHT) / 2;
            int pad = 6;
            int cx = x + pad + indent;

            if (subCount > 0) {
                String arrow = expanded ? "v " : "> ";
                fr.drawStringWithShadow(arrow, cx, textY, 0x7799AA);
                cx += fr.getStringWidth(arrow);
            }

            fr.drawStringWithShadow(statusIcon, cx, textY, iconColor);
            cx += fr.getStringWidth(statusIcon);

            int rightW = fr.getStringWidth(statusLabel);
            if (subCount > 0) rightW += fr.getStringWidth(" (" + subCount + ")") + 4;
            int maxTitleW = width - (cx - x) - pad - fr.getStringWidth(" [" + prioLabel + "]") - rightW - 12;

            String title = task.getTitle();
            if (maxTitleW > 0 && fr.getStringWidth(title) > maxTitleW) {
                while (fr.getStringWidth(title + "..") > maxTitleW && title.length() > 1) {
                    title = title.substring(0, title.length() - 1);
                }
                title += "..";
            }
            boolean done = status == Task.TaskStatus.Completed || status == Task.TaskStatus.Closed;
            fr.drawStringWithShadow(title, cx, textY, done ? 0x888888 : 0xDDDDDD);
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
        int itemBg = depth > 0 ? blendColor(ITEM_BG, depth) : ITEM_BG;
        int itemHover = depth > 0 ? blendColor(ITEM_HOVER, depth) : ITEM_HOVER;
        String shortTitle = task.getTitle()
            .length() > 12 ? task.getTitle()
                .substring(0, 12) : task.getTitle();
        btn.widthRel(1f)
            .height(24)
            .marginTop(1)
            .name("task_list/item_" + shortTitle + "_d" + depth)
            .background(ShaderDrawable.roundedRect(4f, itemBg))
            .hoverBackground(ShaderDrawable.roundedRect(4f, itemHover))
            .overlay(itemOverlay)
            .onMousePressed(b -> {
                if (subCount > 0) {
                    int mx = btn.getContext()
                        .getAbsMouseX();
                    int arrowStartX = btn.getArea().x + 6 + indent;
                    int arrowEndX = arrowStartX + arrowWidth;
                    if (mx >= arrowStartX && mx < arrowEndX) {
                        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                            || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        toggleExpand(task, shift);
                        return true;
                    }
                }
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

    // --- Utilities ---

    private static int blendColor(int base, int depth) {
        int a = (base >>> 24) & 0xFF;
        int r = Math.min(255, ((base >> 16) & 0xFF) + depth * 8);
        int g = Math.min(255, ((base >> 8) & 0xFF) + depth * 8);
        int b = Math.min(255, (base & 0xFF) + depth * 12);
        return (a << 24) | (r << 16) | (g << 8) | b;
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
