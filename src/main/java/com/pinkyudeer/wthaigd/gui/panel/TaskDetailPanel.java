package com.pinkyudeer.wthaigd.gui.panel;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.gui.drawable.ShaderDrawable;
import com.pinkyudeer.wthaigd.gui.widget.MultilineTextField;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.service.TaskService;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TaskDetailPanel extends ModularPanel {

    private static final int FORM_BG = 0x222233D8;
    private static final int INPUT_BG = 0x15152060;
    private static final int ACCENT = 0x99ccffff;
    private static final int BTN_SAVE = 0x336633c0;
    private static final int BTN_DANGER = 0x663333c0;
    private static final int BTN_NEUTRAL = 0x335588a0;
    private static final int SELECTOR_ACTIVE = 0x446688c0;
    private static final int SELECTOR_OPTION = 0x18182840;
    private static final int SELECTOR_HOVER = 0x28304860;
    private static final int SUBTASK_BG = 0x18182840;
    private static final int SUBTASK_HOVER = 0x28304860;

    private final Task task;
    private final Runnable onChanged;
    private final TextFieldWidget titleField;
    private final MultilineTextField descField;

    private Task.TaskStatus selectedStatus;
    private Task.Importance selectedImportance;
    private Task.Urgency selectedUrgency;

    private ListWidget<IWidget, ?> rightList;
    private ListWidget<IWidget, ?> subtaskList;

    private IPanelHandler statusPickerHandler;
    private IPanelHandler importancePickerHandler;
    private IPanelHandler urgencyPickerHandler;
    private IPanelHandler subtaskFormHandler;
    private IPanelHandler subtaskDetailHandler;
    private Task pendingSubtaskDetail;

    private static final Task.TaskStatus[] ALL_STATUSES = { Task.TaskStatus.UnClaimed, Task.TaskStatus.InProgress,
        Task.TaskStatus.Blocked, Task.TaskStatus.Postponed, Task.TaskStatus.Completed, Task.TaskStatus.Closed,
        Task.TaskStatus.Canceled, Task.TaskStatus.Rejected };

    private static final Task.Importance[] ALL_IMPORTANCES = { Task.Importance.LOW, Task.Importance.MEDIUM,
        Task.Importance.HIGH, Task.Importance.CRITICAL };

    private static final Task.Urgency[] ALL_URGENCIES = { Task.Urgency.LOW, Task.Urgency.MEDIUM, Task.Urgency.HIGH,
        Task.Urgency.CRITICAL };

    public TaskDetailPanel(Task task, Runnable onChanged) {
        this(task, onChanged, "wthaigd_task_detail");
    }

    public TaskDetailPanel(Task task, Runnable onChanged, String panelName) {
        super(panelName);
        this.task = task;
        this.onChanged = onChanged;
        this.selectedStatus = task.getStatus();
        this.selectedImportance = task.getImportance();
        this.selectedUrgency = task.getUrgency();

        size(400, 300);
        center();
        background(IDrawable.EMPTY);
        overlay(ShaderDrawable.panel(10f, FORM_BG, ACCENT));

        titleField = new TextFieldWidget();
        titleField.value(new StringValue(task.getTitle() != null ? task.getTitle() : ""));

        descField = new MultilineTextField();
        descField.setFullText(task.getDescription() != null ? task.getDescription() : "");
        descField.hintText("Enter description... (Enter for new line)");

        child(buildContent());
    }

    @Override
    public boolean disablePanelsBelow() {
        return true;
    }

    private Column buildContent() {
        Column root = new Column();
        root.widthRel(1f)
            .heightRel(1f)
            .padding(8)
            .name("detail/root");

        root.child(
            IKey.str("Task Details")
                .color(ACCENT)
                .shadow(true)
                .asWidget()
                .widthRel(1f)
                .height(14)
                .name("detail/header_title"));

        Row body = new Row();
        body.widthRel(1f)
            .heightRel(0.58f)
            .marginTop(2)
            .name("detail/body");
        body.child(buildLeftPane());
        body.child(buildRightPane());
        root.child(body);

        root.child(buildSubtaskSection());
        root.child(buildActions());

        return root;
    }

    private Column buildLeftPane() {
        Column left = new Column();
        left.widthRel(0.72f)
            .heightRel(1f)
            .paddingRight(6)
            .name("detail/left_pane");

        left.child(label("Title", 0));
        titleField.widthRel(1f)
            .height(14)
            .name("detail/left/title_field");
        titleField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        left.child(titleField);

        left.child(label("Description", 2));
        descField.widthRel(1f)
            .heightRel(0.72f)
            .name("detail/left/desc_field");
        descField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        left.child(descField);

        return left;
    }

    private IWidget buildRightPane() {
        rightList = new ListWidget<>();
        rightList.widthRel(0.28f)
            .heightRel(1f)
            .paddingLeft(4)
            .name("detail/right_pane");
        rebuildRightPane();
        return rightList;
    }

    private void rebuildRightPane() {
        rightList.removeAll();

        Task.Priority prio = Task.calculatePriority(selectedImportance, selectedUrgency);
        rightList.child(label("Priority", 0));
        rightList.child(
            IKey.str(prio.name())
                .color(getPriorityColor(prio) | 0xFF000000)
                .asWidget()
                .widthRel(1f)
                .height(10)
                .name("detail/right/priority_value"));

        rightList.child(label("Status", 2));
        rightList.child(
            fieldButton(
                selectedStatus.name(),
                getStatusTextColor(selectedStatus),
                "detail/right/status_btn",
                this::openStatusPicker));

        rightList.child(label("Importance", 2));
        rightList.child(
            fieldButton(
                selectedImportance.name(),
                getImportanceLevelColor(selectedImportance),
                "detail/right/importance_btn",
                this::openImportancePicker));

        rightList.child(label("Urgency", 2));
        rightList.child(
            fieldButton(
                selectedUrgency.name(),
                getUrgencyLevelColor(selectedUrgency),
                "detail/right/urgency_btn",
                this::openUrgencyPicker));

        rightList.scheduleResize();
    }

    private ButtonWidget<?> fieldButton(String text, int color, String widgetName, Runnable onClick) {
        String display = text.length() > 10 ? text.substring(0, 10) : text;
        return new ButtonWidget<>().widthRel(0.95f)
            .height(12)
            .name(widgetName)
            .background(ShaderDrawable.roundedRect(3f, SELECTOR_ACTIVE))
            .hoverBackground(ShaderDrawable.roundedRect(3f, SELECTOR_HOVER))
            .overlay(
                IKey.str(display)
                    .color(color)
                    .scale(0.85f))
            .onMousePressed(btn -> {
                onClick.run();
                return true;
            });
    }

    // --- Picker panels ---

    private void openStatusPicker() {
        if (statusPickerHandler == null) {
            statusPickerHandler = IPanelHandler.simple(
                this,
                (ModularPanel p, EntityPlayer pl) -> buildPickerPanel(
                    "wthaigd_pick_status",
                    "Status",
                    ALL_STATUSES,
                    Enum::name,
                    st -> st == selectedStatus,
                    TaskDetailPanel::getStatusTextColor,
                    st -> {
                        selectedStatus = st;
                        rebuildRightPane();
                    }),
                true);
        } else {
            statusPickerHandler.deleteCachedPanel();
        }
        statusPickerHandler.openPanel();
    }

    private void openImportancePicker() {
        if (importancePickerHandler == null) {
            importancePickerHandler = IPanelHandler.simple(
                this,
                (ModularPanel p, EntityPlayer pl) -> buildPickerPanel(
                    "wthaigd_pick_importance",
                    "Importance",
                    ALL_IMPORTANCES,
                    Enum::name,
                    imp -> imp == selectedImportance,
                    TaskDetailPanel::getImportanceLevelColor,
                    imp -> {
                        selectedImportance = imp;
                        rebuildRightPane();
                    }),
                true);
        } else {
            importancePickerHandler.deleteCachedPanel();
        }
        importancePickerHandler.openPanel();
    }

    private void openUrgencyPicker() {
        if (urgencyPickerHandler == null) {
            urgencyPickerHandler = IPanelHandler.simple(
                this,
                (ModularPanel p, EntityPlayer pl) -> buildPickerPanel(
                    "wthaigd_pick_urgency",
                    "Urgency",
                    ALL_URGENCIES,
                    Enum::name,
                    urg -> urg == selectedUrgency,
                    TaskDetailPanel::getUrgencyLevelColor,
                    urg -> {
                        selectedUrgency = urg;
                        rebuildRightPane();
                    }),
                true);
        } else {
            urgencyPickerHandler.deleteCachedPanel();
        }
        urgencyPickerHandler.openPanel();
    }

    private <T> ModularPanel buildPickerPanel(String panelName, String title, T[] values,
        java.util.function.Function<T, String> labelFn, java.util.function.Predicate<T> isActiveFn,
        java.util.function.Function<T, Integer> colorFn, java.util.function.Consumer<T> onSelect) {

        int itemH = 13;
        int panelH = values.length * itemH + 26;
        ModularPanel picker = new ModularPanel(panelName);
        picker.size(120, panelH);
        picker.center();
        picker.background(IDrawable.EMPTY);
        picker.overlay(ShaderDrawable.panel(6f, 0xFF1E1E38, ACCENT));

        Column col = new Column();
        col.widthRel(1f)
            .heightRel(1f)
            .padding(6)
            .name("picker/" + title + "/root");

        col.child(
            IKey.str(title)
                .color(ACCENT)
                .scale(0.9f)
                .asWidget()
                .widthRel(1f)
                .height(12)
                .name("picker/" + title + "/header"));

        for (T val : values) {
            boolean active = isActiveFn.test(val);
            String name = labelFn.apply(val);
            col.child(
                new ButtonWidget<>().widthRel(0.95f)
                    .height(11)
                    .marginTop(1)
                    .name("picker/" + title + "/opt_" + name)
                    .background(ShaderDrawable.roundedRect(3f, active ? SELECTOR_ACTIVE : SELECTOR_OPTION))
                    .hoverBackground(ShaderDrawable.roundedRect(3f, SELECTOR_HOVER))
                    .overlay(
                        IKey.str(name)
                            .color(active ? colorFn.apply(val) : 0xAAAAAAff)
                            .scale(0.8f))
                    .onMousePressed(btn -> {
                        onSelect.accept(val);
                        picker.closeIfOpen();
                        return true;
                    }));
        }

        picker.child(col);
        return picker;
    }

    // --- Color utilities ---

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

    private static int getStatusTextColor(Task.TaskStatus status) {
        return switch (status) {
            case Completed, Closed -> 0x66CC66ff;
            case InProgress -> 0x44AAFFff;
            case Blocked, Rejected -> 0xFF6666ff;
            case Postponed -> 0xFFAA44ff;
            case Canceled -> 0x888888ff;
            default -> 0xCCCCCCff;
        };
    }

    private static int getImportanceLevelColor(Task.Importance imp) {
        return switch (imp) {
            case CRITICAL -> 0xFF4444ff;
            case HIGH -> 0xFF8844ff;
            case MEDIUM -> 0xFFCC44ff;
            case LOW -> 0x88CC88ff;
            case UNDEFINED -> 0x888888ff;
        };
    }

    private static int getUrgencyLevelColor(Task.Urgency urg) {
        return switch (urg) {
            case CRITICAL -> 0xFF4444ff;
            case HIGH -> 0xFF8844ff;
            case MEDIUM -> 0xFFCC44ff;
            case LOW -> 0x88CC88ff;
            case UNDEFINED -> 0x888888ff;
        };
    }

    // --- Labels ---

    private IWidget label(String text, int topMargin) {
        return IKey.str(text)
            .color(0xAAAAAAff)
            .scale(0.85f)
            .asWidget()
            .widthRel(1f)
            .height(10)
            .marginTop(topMargin);
    }

    // --- Subtask section ---

    private Column buildSubtaskSection() {
        Column section = new Column();
        section.widthRel(1f)
            .height(44)
            .marginTop(2)
            .name("detail/subtask_section");

        Row header = new Row();
        header.widthRel(1f)
            .height(12)
            .name("detail/subtask/header");
        header.child(
            IKey.str("Subtasks")
                .color(0xAAAAAAff)
                .scale(0.85f)
                .asWidget()
                .widthRel(0.6f)
                .heightRel(1f));
        header.child(
            new ButtonWidget<>().width(40)
                .height(11)
                .name("detail/subtask/btn_add")
                .background(ShaderDrawable.roundedRect(3f, BTN_NEUTRAL))
                .hoverBackground(ShaderDrawable.roundedRect(3f, 0x4477aacc))
                .overlay(
                    IKey.str("+ Add")
                        .color(0xCCCCCCff)
                        .scale(0.8f))
                .onMousePressed(btn -> {
                    openSubtaskForm();
                    return true;
                }));
        section.child(header);

        subtaskList = new ListWidget<>();
        subtaskList.widthRel(1f)
            .height(30)
            .name("detail/subtask/list");
        populateSubtasks();
        section.child(subtaskList);
        return section;
    }

    private void populateSubtasks() {
        try {
            List<Task> subs = TaskService.getSubtasks(task.getId());
            if (subs.isEmpty()) {
                subtaskList.child(
                    IKey.str("No subtasks.")
                        .color(0x666666ff)
                        .asWidget()
                        .widthRel(1f)
                        .height(16)
                        .name("detail/subtask/empty_hint"));
            } else {
                for (Task sub : subs) {
                    subtaskList.child(buildSubtaskItem(sub));
                }
            }
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to load subtasks", e);
        }
    }

    private void refreshSubtasks() {
        if (subtaskList == null) return;
        subtaskList.removeAll();
        populateSubtasks();
        subtaskList.scheduleResize();
    }

    private ButtonWidget<?> buildSubtaskItem(Task sub) {
        boolean done = sub.getStatus() == Task.TaskStatus.Completed || sub.getStatus() == Task.TaskStatus.Closed;
        String prefix = done ? "[/] " : "[ ] ";
        int textColor = done ? 0x888888 : 0xCCCCCC;
        String shortTitle = sub.getTitle()
            .length() > 10 ? sub.getTitle()
                .substring(0, 10) : sub.getTitle();
        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.widthRel(1f)
            .height(14)
            .marginTop(1)
            .name("detail/subtask/item_" + shortTitle)
            .background(ShaderDrawable.roundedRect(3f, SUBTASK_BG))
            .hoverBackground(ShaderDrawable.roundedRect(3f, SUBTASK_HOVER))
            .overlay(
                IKey.str(prefix + sub.getTitle())
                    .color(textColor | 0xFF000000)
                    .scale(0.85f))
            .onMousePressed(b -> {
                openSubtaskDetail(sub);
                return true;
            });
        return btn;
    }

    private void openSubtaskForm() {
        if (subtaskFormHandler == null) {
            subtaskFormHandler = IPanelHandler
                .simple(this, (ModularPanel p, EntityPlayer pl) -> new TaskFormPanel(() -> {
                    refreshSubtasks();
                    if (onChanged != null) onChanged.run();
                }, task.getId()), true);
        } else {
            if (subtaskFormHandler.isPanelOpen()) return;
            subtaskFormHandler.deleteCachedPanel();
        }
        subtaskFormHandler.openPanel();
    }

    private void openSubtaskDetail(Task sub) {
        this.pendingSubtaskDetail = sub;
        if (subtaskDetailHandler == null) {
            subtaskDetailHandler = IPanelHandler
                .simple(this, (ModularPanel p, EntityPlayer pl) -> new TaskDetailPanel(pendingSubtaskDetail, () -> {
                    refreshSubtasks();
                    if (onChanged != null) onChanged.run();
                }, "wthaigd_subtask_detail"), true);
        } else {
            if (subtaskDetailHandler.isPanelOpen()) return;
            subtaskDetailHandler.deleteCachedPanel();
        }
        subtaskDetailHandler.openPanel();
    }

    // --- Actions ---

    private Row buildActions() {
        Row actions = new Row();
        actions.widthRel(1f)
            .height(16)
            .marginTop(2)
            .name("detail/actions");

        actions.child(
            new ButtonWidget<>().width(42)
                .height(14)
                .name("detail/actions/btn_save")
                .background(ShaderDrawable.roundedRect(4f, BTN_SAVE))
                .overlay(
                    IKey.str("Save")
                        .color(0xFFFFFFff)
                        .scale(0.9f))
                .onMousePressed(btn -> {
                    saveChanges();
                    return true;
                }));

        boolean isCompleted = task.getStatus() == Task.TaskStatus.Completed
            || task.getStatus() == Task.TaskStatus.Closed;
        if (!isCompleted) {
            actions.child(
                new ButtonWidget<>().width(50)
                    .height(14)
                    .marginLeft(3)
                    .name("detail/actions/btn_complete")
                    .background(ShaderDrawable.roundedRect(4f, BTN_NEUTRAL))
                    .overlay(
                        IKey.str("Complete")
                            .color(0x66CC66ff)
                            .scale(0.9f))
                    .onMousePressed(btn -> {
                        completeAndClose();
                        return true;
                    }));
        }

        actions.child(
            new ButtonWidget<>().width(42)
                .height(14)
                .marginLeft(3)
                .name("detail/actions/btn_delete")
                .background(ShaderDrawable.roundedRect(4f, BTN_DANGER))
                .overlay(
                    IKey.str("Delete")
                        .color(0xFF6666ff)
                        .scale(0.9f))
                .onMousePressed(btn -> {
                    deleteAndClose();
                    return true;
                }));

        actions.child(
            new ButtonWidget<>().width(36)
                .height(14)
                .marginLeft(3)
                .name("detail/actions/btn_close")
                .background(ShaderDrawable.roundedRect(4f, SELECTOR_OPTION))
                .overlay(
                    IKey.str("Close")
                        .color(0xCCCCCCff)
                        .scale(0.9f))
                .onMousePressed(btn -> {
                    closeIfOpen();
                    return true;
                }));

        return actions;
    }

    // --- Save / Complete / Delete ---

    private void saveChanges() {
        try {
            String newTitle = titleField.getText();
            String newDesc = descField.getFullText();
            if (newTitle == null || newTitle.trim()
                .isEmpty()) return;

            Task oldTask = com.pinkyudeer.wthaigd.helper.UtilHelper.deepClone(task, Task.class);
            task.setTitle(newTitle.trim());
            task.setDescription(newDesc != null ? newDesc.trim() : "");

            if (selectedImportance != oldTask.getImportance()) {
                task.setImportance(selectedImportance);
            }
            if (selectedUrgency != oldTask.getUrgency()) {
                task.setUrgency(selectedUrgency);
            }
            if (selectedImportance != oldTask.getImportance() || selectedUrgency != oldTask.getUrgency()) {
                task.setPriority(Task.calculatePriority(selectedImportance, selectedUrgency));
            }

            if (selectedStatus != oldTask.getStatus()) {
                UUID operatorId = Minecraft.getMinecraft().thePlayer.getUniqueID();
                TaskService.changeStatus(task.getId(), selectedStatus, operatorId);
            }
            TaskService.updateTask(task, oldTask);
            closeIfOpen();
            if (onChanged != null) onChanged.run();
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to save task changes", e);
        }
    }

    private void completeAndClose() {
        try {
            UUID operatorId = Minecraft.getMinecraft().thePlayer.getUniqueID();
            TaskService.completeTask(task.getId(), operatorId);
            closeIfOpen();
            if (onChanged != null) onChanged.run();
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to complete task", e);
        }
    }

    private void deleteAndClose() {
        try {
            TaskService.deleteTask(task.getId());
            closeIfOpen();
            if (onChanged != null) onChanged.run();
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to delete task", e);
        }
    }
}
