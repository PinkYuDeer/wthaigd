package com.pinkyudeer.wthaigd.task;

import com.pinkyudeer.wthaigd.helper.SQLiteHelper;
import com.pinkyudeer.wthaigd.task.entity.Player;

public class TaskEntityManager {
    // TODO 在taskManager中实现增删改查与结果显示，将实体类的属性变为private，以getter和setter方法的形式访问

    public static void initDatabase() {
        // 执行所有建表语句
        for (String sql : TaskSqlHelper.init.CREATE_TABLES) {
            SQLiteHelper.executeSafeSQL(sql);
        }
    }

    public static class add {

        public static void player(Player player) {}
    }
}
