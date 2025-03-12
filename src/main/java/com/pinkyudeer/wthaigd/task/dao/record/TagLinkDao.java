package com.pinkyudeer.wthaigd.task.dao.record;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.record.TagLink;

public class TagLinkDao {

    public static Integer insert(TagLink tagLink) {
        return SQLHelper.insert(tagLink);
    }

    public static UpdateBuilder<TagLink> update() {
        return SQLHelper.update(TagLink.class);
    }

    public static Integer updateByIdByCompare(TagLink tagLink, TagLink oldTagLink) {
        return SQLHelper.updateByCompare(tagLink, oldTagLink)
            .byId()
            .execute();
    }

    public static DeleteBuilder<TagLink> delete() {
        return SQLHelper.delete(TagLink.class);
    }

    public static Integer deleteById(TagLink tagLink) {
        return SQLHelper.deleteById(tagLink)
            .execute();
    }

    public static SelectBuilder<TagLink> select() {
        return SQLHelper.select(TagLink.class);
    }

    public static List<TagLink> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(TagLink.class);
    }
}
