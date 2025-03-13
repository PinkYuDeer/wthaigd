package com.pinkyudeer.wthaigd.task;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

import org.reflections.Reflections;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;
import com.pinkyudeer.wthaigd.task.dao.PlayerDao;

/**
 * 任务系统数据库操作助手类
 * 提供任务相关实体的CRUD操作
 */
public class TaskSqlHelper {

    /**
     * 初始化任务数据库
     * 扫描并创建所有任务相关的表
     */
    public static void initTaskDataBase() {
        Reflections reflections = new Reflections("com.pinkyudeer.wthaigd.task.entity");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Table.class);
        try {
            SQLHelper.createTables(annotatedClasses);
        } catch (Exception e) {
            Wthaigd.LOG.error("初始化任务数据库失败", e);
            return;
        }
        Wthaigd.LOG.info("初始化任务数据库，共创建 {} 张表", annotatedClasses.size());
    }

    public static class player {

        public static void login(EntityPlayer player) {
            PlayerDao.updateOrInsert(player);
        }
    }
}
