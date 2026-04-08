package com.pinkyudeer.wthaigd.gui.panel;

import java.util.UUID;

import net.minecraft.client.Minecraft;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.pinkyudeer.wthaigd.gui.drawable.ShaderDrawable;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.service.TaskService;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TaskFormPanel extends ModularPanel {

    private static final int FORM_BG = 0x22223360;
    private static final int INPUT_BG = 0x15152040;
    private static final int ACCENT = 0x99ccffff;
    private static final int BTN_SAVE = 0x336633c0;
    private static final int BTN_CANCEL = 0x663333c0;

    private final TextFieldWidget titleField;
    private final TextFieldWidget descField;
    private Task.Importance selectedImportance = Task.Importance.MEDIUM;
    private Task.Urgency selectedUrgency = Task.Urgency.MEDIUM;

    private TaskFormPanel() {
        super("task_form");
        size(260, 200);
        center();
        background(IDrawable.EMPTY);
        overlay(ShaderDrawable.panel(10f, FORM_BG, ACCENT));

        titleField = new TextFieldWidget();
        descField = new TextFieldWidget();

        child(buildForm());
    }

    public static void openCreate(ModularScreen screen) {
        TaskFormPanel panel = new TaskFormPanel();
        screen.getPanelManager()
            .openPanel(panel, null);
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

        form.child(buildImportanceRow());
        form.child(buildUrgencyRow());
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

        Task.Importance[] values = { Task.Importance.LOW, Task.Importance.MEDIUM, Task.Importance.HIGH };
        for (Task.Importance imp : values) {
            boolean active = imp == selectedImportance;
            row.child(
                new ButtonWidget<>().width(40)
                    .height(16)
                    .marginLeft(4)
                    .background(ShaderDrawable.roundedRect(4f, active ? 0x446688c0 : 0x22334460))
                    .overlay(
                        IKey.str(
                            imp.name()
                                .substring(0, 3))
                            .color(active ? 0xFFFFFFff : 0x999999ff))
                    .onMousePressed(btn -> {
                        selectedImportance = imp;
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

        Task.Urgency[] values = { Task.Urgency.LOW, Task.Urgency.MEDIUM, Task.Urgency.HIGH };
        for (Task.Urgency urg : values) {
            boolean active = urg == selectedUrgency;
            row.child(
                new ButtonWidget<>().width(40)
                    .height(16)
                    .marginLeft(4)
                    .background(ShaderDrawable.roundedRect(4f, active ? 0x446688c0 : 0x22334460))
                    .overlay(
                        IKey.str(
                            urg.name()
                                .substring(0, 3))
                            .color(active ? 0xFFFFFFff : 0x999999ff))
                    .onMousePressed(btn -> {
                        selectedUrgency = urg;
                        return true;
                    }));
        }
        return row;
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
        TaskService.createTask(title.trim(), desc.trim(), creatorId, selectedImportance, selectedUrgency);

        getScreen().close();
    }
}
