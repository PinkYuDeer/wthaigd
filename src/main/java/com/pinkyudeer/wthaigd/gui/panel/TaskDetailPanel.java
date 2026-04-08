package com.pinkyudeer.wthaigd.gui.panel;

import java.util.UUID;

import net.minecraft.client.Minecraft;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
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

    private final Task task;
    private final Runnable onChanged;
    private final TextFieldWidget titleField;
    private final TextFieldWidget descField;
    private Task.TaskStatus selectedStatus;

    public TaskDetailPanel(Task task, Runnable onChanged) {
        super("task_detail_" + task.getId());
        this.task = task;
        this.onChanged = onChanged;
        this.selectedStatus = task.getStatus();

        size(300, 260);
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
        Column col = new Column();
        col.widthRel(1f)
            .heightRel(1f)
            .padding(12);

        col.child(
            IKey.str("Task Details")
                .color(ACCENT)
                .shadow(true)
                .asWidget()
                .widthRel(1f)
                .height(18));

        col.child(label("Title", 8));
        titleField.widthRel(0.95f)
            .height(18);
        titleField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        col.child(titleField);

        col.child(label("Description", 6));
        descField.widthRel(0.95f)
            .height(18);
        descField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        col.child(descField);

        col.child(label("Status", 6));
        col.child(buildStatusRow());

        col.child(buildInfoRow());
        col.child(buildActions());

        return col;
    }

    private IWidget label(String text, int topMargin) {
        return IKey.str(text)
            .color(0xAAAAAAff)
            .asWidget()
            .widthRel(1f)
            .height(14)
            .marginTop(topMargin);
    }

    private Row buildStatusRow() {
        Row row = new Row();
        row.widthRel(1f)
            .height(22)
            .marginTop(2);

        Task.TaskStatus[] statuses = { Task.TaskStatus.UnClaimed, Task.TaskStatus.InProgress, Task.TaskStatus.Completed,
            Task.TaskStatus.Closed };
        for (Task.TaskStatus st : statuses) {
            boolean active = st == selectedStatus;
            String lbl = st.name()
                .length() > 6 ? st.name()
                    .substring(0, 6) : st.name();
            row.child(
                new ButtonWidget<>().width(50)
                    .height(16)
                    .marginLeft(3)
                    .background(ShaderDrawable.roundedRect(4f, active ? STATUS_ACTIVE : STATUS_BG))
                    .overlay(
                        IKey.str(lbl)
                            .color(active ? 0xFFFFFFff : 0x999999ff))
                    .onMousePressed(btn -> {
                        selectedStatus = st;
                        return true;
                    }));
        }
        return row;
    }

    private Row buildInfoRow() {
        Row row = new Row();
        row.widthRel(1f)
            .height(18)
            .marginTop(6);

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

    private Row buildActions() {
        Row actions = new Row();
        actions.widthRel(1f)
            .height(24)
            .marginTop(8);

        actions.child(
            new ButtonWidget<>().width(60)
                .height(22)
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
                new ButtonWidget<>().width(70)
                    .height(22)
                    .marginLeft(6)
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
            new ButtonWidget<>().width(60)
                .height(22)
                .marginLeft(6)
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
            new ButtonWidget<>().width(50)
                .height(22)
                .marginLeft(6)
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
