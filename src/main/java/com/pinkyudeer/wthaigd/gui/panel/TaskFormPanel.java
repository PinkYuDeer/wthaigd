package com.pinkyudeer.wthaigd.gui.panel;

import java.util.UUID;

import net.minecraft.client.Minecraft;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
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
public class TaskFormPanel extends ModularPanel {

    private static final int FORM_BG = 0x222233D8;
    private static final int INPUT_BG = 0x15152060;
    private static final int ACCENT = 0x99ccffff;
    private static final int BTN_SAVE = 0x336633c0;
    private static final int BTN_CANCEL = 0x663333c0;

    private static final int TOGGLE_ACTIVE = 0x446688c0;
    private static final int TOGGLE_INACTIVE = 0x22334460;

    private final TextFieldWidget titleField;
    private final TextFieldWidget descField;
    private final Runnable onSaved;
    private Task.Importance selectedImportance = Task.Importance.MEDIUM;
    private Task.Urgency selectedUrgency = Task.Urgency.MEDIUM;

    private Row importanceRow;
    private Row urgencyRow;

    public TaskFormPanel(Runnable onSaved) {
        super("task_form");
        this.onSaved = onSaved;
        size(260, 220);
        center();
        background(IDrawable.EMPTY);
        overlay(ShaderDrawable.panel(10f, FORM_BG, ACCENT));

        titleField = new TextFieldWidget();
        descField = new TextFieldWidget();

        child(buildForm());
    }

    @Override
    public boolean disablePanelsBelow() {
        return true;
    }

    private Column buildForm() {
        Column form = new Column();
        form.widthRel(1f)
            .heightRel(1f);
        form.padding(12);

        form.child(
            IKey.str("Create Task")
                .color(ACCENT)
                .shadow(true)
                .asWidget()
                .widthRel(1f)
                .height(20));

        form.child(
            IKey.str("Title")
                .color(0xCCCCCCff)
                .asWidget()
                .widthRel(1f)
                .height(14)
                .marginTop(8));

        titleField.widthRel(0.95f)
            .height(18);
        titleField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        form.child(titleField);

        form.child(
            IKey.str("Description")
                .color(0xCCCCCCff)
                .asWidget()
                .widthRel(1f)
                .height(14)
                .marginTop(6));

        descField.widthRel(0.95f)
            .height(18);
        descField.background(ShaderDrawable.roundedRect(4f, INPUT_BG));
        form.child(descField);

        importanceRow = buildImportanceRow();
        form.child(importanceRow);
        urgencyRow = buildUrgencyRow();
        form.child(urgencyRow);
        form.child(buildActions());

        return form;
    }

    private Row buildImportanceRow() {
        Row row = new Row();
        row.widthRel(1f)
            .height(22)
            .marginTop(6);
        row.child(
            IKey.str("Importance: ")
                .color(0xAAAAAAff)
                .asWidget()
                .width(80)
                .heightRel(1f));

        Task.Importance[] values = { Task.Importance.LOW, Task.Importance.MEDIUM, Task.Importance.HIGH,
            Task.Importance.CRITICAL };
        for (Task.Importance imp : values) {
            boolean active = imp == selectedImportance;
            boolean crit = imp == Task.Importance.CRITICAL;
            int activeBg = crit ? 0xCC3333cc : TOGGLE_ACTIVE;
            row.child(
                new ButtonWidget<>().width(crit ? 44 : 36)
                    .height(16)
                    .marginLeft(4)
                    .background(ShaderDrawable.roundedRect(4f, active ? activeBg : TOGGLE_INACTIVE))
                    .overlay(
                        IKey.str(
                            crit ? "CRIT"
                                : imp.name()
                                    .substring(0, 3))
                            .color(active ? 0xFFFFFFff : (crit ? 0xFF6666ff : 0x999999ff)))
                    .onMousePressed(btn -> {
                        selectedImportance = imp;
                        rebuildToggles();
                        return true;
                    }));
        }
        return row;
    }

    private Row buildUrgencyRow() {
        Row row = new Row();
        row.widthRel(1f)
            .height(22)
            .marginTop(4);
        row.child(
            IKey.str("Urgency: ")
                .color(0xAAAAAAff)
                .asWidget()
                .width(80)
                .heightRel(1f));

        Task.Urgency[] values = { Task.Urgency.LOW, Task.Urgency.MEDIUM, Task.Urgency.HIGH, Task.Urgency.CRITICAL };
        for (Task.Urgency urg : values) {
            boolean active = urg == selectedUrgency;
            boolean crit = urg == Task.Urgency.CRITICAL;
            int activeBg = crit ? 0xCC3333cc : TOGGLE_ACTIVE;
            row.child(
                new ButtonWidget<>().width(crit ? 44 : 36)
                    .height(16)
                    .marginLeft(4)
                    .background(ShaderDrawable.roundedRect(4f, active ? activeBg : TOGGLE_INACTIVE))
                    .overlay(
                        IKey.str(
                            crit ? "CRIT"
                                : urg.name()
                                    .substring(0, 3))
                            .color(active ? 0xFFFFFFff : (crit ? 0xFF6666ff : 0x999999ff)))
                    .onMousePressed(btn -> {
                        selectedUrgency = urg;
                        rebuildToggles();
                        return true;
                    }));
        }
        return row;
    }

    private void rebuildToggles() {
        Column form = (Column) getChildren().get(0);
        int impIdx = form.getChildren()
            .indexOf(importanceRow);
        int urgIdx = form.getChildren()
            .indexOf(urgencyRow);

        if (impIdx >= 0) {
            form.remove(impIdx);
            importanceRow = buildImportanceRow();
            form.addChild(importanceRow, impIdx);
        }
        if (urgIdx >= 0) {
            int adjustedIdx = urgIdx > impIdx ? urgIdx : urgIdx + 1;
            form.remove(adjustedIdx);
            urgencyRow = buildUrgencyRow();
            form.addChild(urgencyRow, adjustedIdx);
        }

        form.scheduleResize();
    }

    private Row buildActions() {
        Row actions = new Row();
        actions.widthRel(1f)
            .height(24)
            .marginTop(10);

        actions.child(
            new ButtonWidget<>().widthRel(0.45f)
                .height(22)
                .background(ShaderDrawable.roundedRect(6f, BTN_SAVE))
                .overlay(
                    IKey.str("Save")
                        .color(0xFFFFFFff)
                        .shadow(true))
                .onMousePressed(btn -> {
                    saveTask();
                    return true;
                }));

        actions.child(
            new ButtonWidget<>().widthRel(0.45f)
                .height(22)
                .marginLeft(8)
                .background(ShaderDrawable.roundedRect(6f, BTN_CANCEL))
                .overlay(
                    IKey.str("Cancel")
                        .color(0xFFFFFFff)
                        .shadow(true))
                .onMousePressed(btn -> {
                    closeIfOpen();
                    return true;
                }));

        return actions;
    }

    private void saveTask() {
        String title = titleField.getText();
        String desc = descField.getText();

        if (title == null || title.trim()
            .isEmpty()) return;
        if (desc == null) desc = "";

        UUID creatorId = Minecraft.getMinecraft().thePlayer.getUniqueID();

        try {
            Task task = TaskService
                .createTask(title.trim(), desc.trim(), creatorId, selectedImportance, selectedUrgency);
            if (task != null) {
                closeIfOpen();
                if (onSaved != null) onSaved.run();
            }
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to save task from GUI", e);
        }
    }
}
