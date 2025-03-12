package com.pinkyudeer.wthaigd.task.dao;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.Tag;

public class TagDao {

    public static Integer insert(Tag tag) {
        return SQLHelper.insert(tag);
    }

    public static UpdateBuilder<Tag> update() {
        return SQLHelper.update(Tag.class);
    }

    public static Integer updateByIdByCompare(Tag tag, Tag oldTag) {
        return SQLHelper.updateByCompare(tag, oldTag)
            .byId()
            .execute();
    }

    public static DeleteBuilder<Tag> delete() {
        return SQLHelper.delete(Tag.class);
    }

    public static Integer deleteById(Tag tag) {
        return SQLHelper.deleteById(tag)
            .execute();
    }

    public static SelectBuilder<Tag> select() {
        return SQLHelper.select(Tag.class);
    }

    public static List<Tag> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(Tag.class);
    }
}
