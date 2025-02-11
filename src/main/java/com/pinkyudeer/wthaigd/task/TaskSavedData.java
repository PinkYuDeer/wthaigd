package com.pinkyudeer.wthaigd.task;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.TaskUser;
import com.pinkyudeer.wthaigd.task.entity.TaskUserGroup;

// 包含任务、玩家状态的文件【WIP，已被sqlite替代】
public class TaskSavedData extends WorldSavedData {

    private static final String DATA_NAME = "wthaigd";
    @SuppressWarnings("unused")
    private List<TaskUserGroup> userGroups; // 用户组列表
    @SuppressWarnings("unused")
    private List<TaskUser> users; // 用户列表
    @SuppressWarnings("unused")
    private List<Task> tasks; // 任务列表

    public TaskSavedData() {
        super(DATA_NAME);
    }

    public TaskSavedData(String s) {
        super(s);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {

    }

    // 获取数据
    public static TaskSavedData get(World world) {
        MapStorage storage = world.mapStorage;
        TaskSavedData instance = (TaskSavedData) storage.loadData(TaskSavedData.class, DATA_NAME);
        if (instance == null) {
            instance = new TaskSavedData();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
}
