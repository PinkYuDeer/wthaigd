package com.pinkyudeer.wthaigd.task.dao.record;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.db.SQLHelper;
import com.pinkyudeer.wthaigd.db.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.db.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.db.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.record.TaskInteraction;

public class TaskInteractionDao {

    public static Integer insert(TaskInteraction interaction) {
        return SQLHelper.insert(interaction);
    }

    public static UpdateBuilder<TaskInteraction> update() {
        return SQLHelper.update(TaskInteraction.class);
    }

    public static Integer updateByIdByCompare(TaskInteraction interaction, TaskInteraction oldInteraction) {
        return SQLHelper.updateByCompare(interaction, oldInteraction)
            .byId()
            .execute();
    }

    public static DeleteBuilder<TaskInteraction> delete() {
        return SQLHelper.delete(TaskInteraction.class);
    }

    public static Integer deleteById(TaskInteraction interaction) {
        return SQLHelper.deleteById(interaction)
            .execute();
    }

    public static SelectBuilder<TaskInteraction> select() {
        return SQLHelper.select(TaskInteraction.class);
    }

    public static List<TaskInteraction> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(TaskInteraction.class);
    }
}
