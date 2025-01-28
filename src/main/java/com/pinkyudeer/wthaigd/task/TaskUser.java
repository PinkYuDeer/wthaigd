package com.pinkyudeer.wthaigd.task;

public class TaskUser {

    private String username; // 玩家名
    private String UUID; // 玩家UUID

    private int points; // 玩家积分
    private int level; // 玩家等级
    private int editPoints; // 玩家编辑积分
    private int workPoints; // 玩家工作积分
    private int taskCreated; // 玩家创建任务数

    private int taskDone; // 玩家完成任务数
    private int taskGet; // 玩家领取任务数
    private int taskFollowed; // 玩家关注任务数
    private int taskLiked; // 玩家点赞任务数
    private int taskCommented; // 玩家评论任务数

    private int taskBeDone; // 玩家任务被完成数
    private int taskBeGet; // 玩家任务被领取数
    private int taskBeFollowed; // 玩家任务被关注数
    private int taskBeLiked; // 玩家任务被点赞数
    private int taskBeCommented; // 玩家任务被评论数

    public TaskUser(String username, String UUID) {
        this.username = username;
        this.UUID = UUID;
        this.points = 0;
        this.level = 0;
        this.editPoints = 0;
        this.workPoints = 0;
        this.taskCreated = 0;
        this.taskDone = 0;
        this.taskGet = 0;
        this.taskFollowed = 0;
        this.taskLiked = 0;
        this.taskCommented = 0;
        this.taskBeDone = 0;
        this.taskBeGet = 0;
        this.taskBeFollowed = 0;
        this.taskBeLiked = 0;
        this.taskBeCommented = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEditPoints() {
        return editPoints;
    }

    public void setEditPoints(int editPoints) {
        this.editPoints = editPoints;
    }

    public int getWorkPoints() {
        return workPoints;
    }

    public void setWorkPoints(int workPoints) {
        this.workPoints = workPoints;
    }

    public int getTaskCreated() {
        return taskCreated;
    }

    public void setTaskCreated(int taskCreated) {
        this.taskCreated = taskCreated;
    }

    public int getTaskDone() {
        return taskDone;
    }

    public void setTaskDone(int taskDone) {
        this.taskDone = taskDone;
    }

    public int getTaskGet() {
        return taskGet;
    }

    public void setTaskGet(int taskGet) {
        this.taskGet = taskGet;
    }

    public int getTaskFollowed() {
        return taskFollowed;
    }

    public void setTaskFollowed(int taskFollowed) {
        this.taskFollowed = taskFollowed;
    }

    public int getTaskLiked() {
        return taskLiked;
    }

    public void setTaskLiked(int taskLiked) {
        this.taskLiked = taskLiked;
    }

    public int getTaskCommented() {
        return taskCommented;
    }

    public void setTaskCommented(int taskCommented) {
        this.taskCommented = taskCommented;
    }

    public int getTaskBeDone() {
        return taskBeDone;
    }

    public void setTaskBeDone(int taskBeDone) {
        this.taskBeDone = taskBeDone;
    }

    public int getTaskBeGet() {
        return taskBeGet;
    }

    public void setTaskBeGet(int taskBeGet) {
        this.taskBeGet = taskBeGet;
    }

    public int getTaskBeFollowed() {
        return taskBeFollowed;
    }

    public void setTaskBeFollowed(int taskBeFollowed) {
        this.taskBeFollowed = taskBeFollowed;
    }

    public int getTaskBeLiked() {
        return taskBeLiked;
    }

    public void setTaskBeLiked(int taskBeLiked) {
        this.taskBeLiked = taskBeLiked;
    }

    public int getTaskBeCommented() {
        return taskBeCommented;
    }

    public void setTaskBeCommented(int taskBeCommented) {
        this.taskBeCommented = taskBeCommented;
    }
}
