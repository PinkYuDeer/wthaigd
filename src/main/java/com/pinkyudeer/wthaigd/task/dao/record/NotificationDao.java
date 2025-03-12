package com.pinkyudeer.wthaigd.task.dao.record;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.record.Notification;

public class NotificationDao {

    public static Integer insert(Notification notification) {
        return SQLHelper.insert(notification);
    }

    public static UpdateBuilder<Notification> update() {
        return SQLHelper.update(Notification.class);
    }

    public static Integer updateByIdByCompare(Notification notification, Notification oldNotification) {
        return SQLHelper.updateByCompare(notification, oldNotification)
            .byId()
            .execute();
    }

    public static DeleteBuilder<Notification> delete() {
        return SQLHelper.delete(Notification.class);
    }

    public static Integer deleteById(Notification notification) {
        return SQLHelper.deleteById(notification)
            .execute();
    }

    public static SelectBuilder<Notification> select() {
        return SQLHelper.select(Notification.class);
    }

    public static List<Notification> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(Notification.class);
    }
}
