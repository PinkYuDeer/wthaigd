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
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.gui.drawable.ShaderDrawable;
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
    private static final int STATUS_BG = 0x22334460;
    private static final int STATUS_ACTIVE = 0x446688c0;
    private static final int SUBTASK_BG = 0x18182840;
    private static final int SUBTASK_HOVER = 0x28304860;

    private final Task task;
    private final Runnable onChanged;
    private final TextFieldWidget titleField;
    private final TextFieldWidget descField;
    private Task.TaskStatus selectedStatus;

    private Column contentCol;
    private Row statusRow;
    private ListWidget<IWidget, ?> subtaskList;

    private static final Task.TaskStatus[] STATUS_ROW_1 = { Task.TaskStatus.UnClaimed, Task.TaskStatus.InProgress,
        Task.TaskStatus.Blocked, Task.TaskStatus.Postponed };
    private static final Task.TaskStatus[] STATUS_ROW_2 = { Task.TaskStatus.Completed, Task.TaskStatus.Closed,
        Task.TaskStatus.Canceled, Task.TaskStatus.Rejected };

    public TaskDetailPanel(Task task, Runnable onChanged) {
        super("task_detail_" + task.getId());
        this.task = task;
        this.onChanged = onChanged;
        this.selectedStatus = task.getStatus();

        size(320, 340);
        center();
        background(IDrawable.EMPTY);
        overlay(ShaderDrawable.panel(10f, FORM_BG, ACCENT));

        titleField = new TextFieldWidget();
        descField = new TextFieldWidget();

        child(buildContent());
    }

    @Override
    public void afterInit() {
        super.afterInit();
        titleField.setText(task.getTitle() != null ? task.getTitle() : "");
        descField.setText(task.getDescription() != null ? task.getDescription() : "");
    }

    @Override
    public boolean disablePanelsBelow() {
        return true;
    }

    private Column buildContent() {
        contentCol = new Column();
        contentCol.widthRel(1f)
            .heightRel(1f)
            .padding(12);

        contentCol.child(
            IKey.str("Task Details")
                .color(ACCENT)
                .shadow(true)
                .asWidget()
                .widthRel(1f)
                .height(16));

        contentCol.child(label("Title", 4));
        titleField.widthRel(0.95f)
            .height(16);
        titleField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        contentCol.child(titleField);

        contentCol.child(label("Description", 4));
        descField.widthRel(0.95f)
            .height(16);
        descField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        contentCol.child(descField);

        contentCol.child(label("Status", 4));
        statusRow = buildStatusRows();
        contentCol.child(statusRow);

        contentCol.child(buildInfoRow());

        contentCol.child(buildSubtaskSection());

        contentCol.child(buildActions());

        return contentCol;
    }

    private IWidget label(String text, int topMargin) {
        return IKey.str(text)
            .color(0xAAAAAAff)
            .asWidget()
            .widthRel(1f)
            .height(12)
            .marginTop(topMargin);
    }

    private Row buildStatusRows() {
        Row container = new Row();
        container.widthRel(1f)
            .height(36)
            .marginTop(2);

        Column rows = new Column();
        rows.widthRel(1f)
            .heightRel(1f);

        Row row1 = new Row();
        row1.widthRel(1f)
            .height(16);
        for (Task.TaskStatus st : STATUS_ROW_1) {
            row1.child(buildStatusBtn(st));
        }
        rows.child(row1);

        Row row2 = new Row();
        row2.widthRel(1f)
            .height(16)
            .marginTop(2);
        for (Task.TaskStatus st : STATUS_ROW_2) {
            row2.child(buildStatusBtn(st));
        }
        rows.child(row2);

        container.child(rows);
        return container;
    }

    private ButtonWidget<?> buildStatusBtn(Task.TaskStatus st) {
        boolean active = st == selectedStatus;
        String lbl = st.name()
            .length() > 6 ? st.name()
                .substring(0, 6) : st.name();
        return new ButtonWidget<>().width(56)
            .height(14)
            .marginLeft(3)
            .background(ShaderDrawable.roundedRect(3f, active ? STATUS_ACTIVE : STATUS_BG))
            .overlay(
                IKey.str(lbl)
                    .color(active ? 0xFFFFFFff : 0x999999ff)
                    .scale(0.85f))
            .onMousePressed(btn -> {
                selectedStatus = st;
                rebuildStatusRows();
                return true;
            });
    }

    private void rebuildStatusRows() {
        if (contentCol == null || statusRow == null) return;
        int idx = contentCol.getChildren()
            .indexOf(statusRow);
        if (idx < 0) return;
        contentCol.getChildren()
            .remove(idx);
        statusRow = buildStatusRows();
        contentCol.getChildren()
            .add(idx, statusRow);
        contentCol.scheduleResize();
    }

    private int getStatusColor(Task.TaskStatus status) {
        return switch (status) {
            case InProgress -> 0x44AAFF;
            case Completed -> 0x66CC66;
            case Closed -> 0x666666;
            case Canceled, Rejected -> 0xCC4444;
            case Blocked -> 0xFF8844;
            case Postponed -> 0xAAAA44;
            case Defect -> 0xFF6644;
            case InTrialRun -> 0x88CCAA;
            default -> 0xAAAAAA;
        };
    }

    private Row buildInfoRow() {
        Row row = new Row();
        row.widthRel(1f)
            .height(14)
            .marginTop(4);

        String prio = "Priority: " + task.getPriority()
            .name();
        String imp = "  Imp: " + task.getImportance()
            .name();
        String urg = "  Urg: " + task.getUrgency()
            .name();

        row.child(
            IKey.str(prio + imp + urg)
                .color(0x888888ff)
                .asWidget()
                .widthRel(1f)
                .heightRel(1f));

        return row;
    }

    private Column buildSubtaskSection() {
        Column section = new Column();
        section.widthRel(1f)
            .height(80)
            .marginTop(4);

        Row header = new Row();
        header.widthRel(1f)
            .height(16);
        header.child(
            IKey.str("Subtasks")
                .color(0xAAAAAAff)
                .asWidget()
                .widthRel(0.6f)
                .heightRel(1f));
        header.child(
            new ButtonWidget<>().width(50)
                .height(14)
                .background(ShaderDrawable.roundedRect(3f, BTN_NEUTRAL))
                .hoverBackground(ShaderDrawable.roundedRect(3f, 0x4477aacc))
                .overlay(
                    IKey.str("+ Add")
                        .color(0xCCCCCCff)
                        .scale(0.85f))
                .onMousePressed(btn -> {
                    openSubtaskForm();
                    return true;
                }));
        section.child(header);

        subtaskList = new ListWidget<>();
        subtaskList.widthRel(1f)
            .height(60);
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
                        .height(18));
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
        String prefix = done ? "[x] " : "[ ] ";
        int textColor = done ? 0x888888 : 0xCCCCCC;

        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.widthRel(1f)
            .height(16)
            .marginTop(1)
            .background(ShaderDrawable.roundedRect(3f, SUBTASK_BG))
            .hoverBackground(ShaderDrawable.roundedRect(3f, SUBTASK_HOVER))
            .overlay(
                IKey.str(prefix + sub.getTitle())
                    .color(textColor | 0xFF000000)
                    .scale(0.9f))
            .onMousePressed(b -> {
                openSubtaskDetail(sub);
                return true;
            });
        return btn;
    }

    private void openSubtaskForm() {
        IPanelHandler handler = IPanelHandler
            .simple(this, (ModularPanel parentPanel, EntityPlayer player) -> new TaskFormPanel(() -> {
                refreshSubtasks();
                if (onChanged != null) onChanged.run();
            }, task.getId()), true);
        handler.openPanel();
    }

    private void openSubtaskDetail(Task sub) {
        IPanelHandler handler = IPanelHandler
            .simple(this, (ModularPanel parentPanel, EntityPlayer player) -> new TaskDetailPanel(sub, () -> {
                refreshSubtasks();
                if (onChanged != null) onChanged.run();
            }), true);
        handler.openPanel();
    }

    private Row buildActions() {
        Row actions = new Row();
        actions.widthRel(1f)
            .height(22)
            .marginTop(6);

        actions.child(
            new ButtonWidget<>().width(55)
                .height(20)
                .background(ShaderDrawable.roundedRect(6f, BTN_SAVE))
                .overlay(
                    IKey.str("Save")
                        .color(0xFFFFFFff)
                        .shadow(true))
                .onMousePressed(btn -> {
                    saveChanges();
                    return true;
                }));

        boolean isCompleted = task.getStatus() == Task.TaskStatus.Completed
            || task.getStatus() == Task.TaskStatus.Closed;
        if (!isCompleted) {
            actions.child(
                new ButtonWidget<>().width(60)
                    .height(20)
                    .marginLeft(4)
                    .background(ShaderDrawable.roundedRect(6f, BTN_NEUTRAL))
                    .overlay(
                        IKey.str("Complete")
                            .color(0x66CC66ff)
                            .shadow(true))
                    .onMousePressed(btn -> {
                        completeAndClose();
                        return true;
                    }));
        }

        actions.child(
            new ButtonWidget<>().width(50)
                .height(20)
                .marginLeft(4)
                .background(ShaderDrawable.roundedRect(6f, BTN_DANGER))
                .overlay(
                    IKey.str("Delete")
                        .color(0xFF6666ff)
                        .shadow(true))
                .onMousePressed(btn -> {
                    deleteAndClose();
                    return true;
                }));

        actions.child(
            new ButtonWidget<>().width(45)
                .height(20)
                .marginLeft(4)
                .background(ShaderDrawable.roundedRect(6f, STATUS_BG))
                .overlay(
                    IKey.str("Close")
                        .color(0xCCCCCCff))
                .onMousePressed(btn -> {
                    closeIfOpen();
                    return true;
                }));

        return actions;
    }

    private void saveChanges() {
        try {
            String newTitle = titleField.getText();
            String newDesc = descField.getText();

            if (newTitle == null || newTitle.trim()
                .isEmpty()) return;

            Task oldTask = com.pinkyudeer.wthaigd.helper.UtilHelper.deepClone(task, Task.class);

            task.setTitle(newTitle.trim());
            task.setDescription(newDesc != null ? newDesc.trim() : "");

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
