package com.pinkyudeer.wthaigd.task;

import java.util.List;

public class TaskUserGroup {

    private String groupName; // 用户组名
    private int groupLevel; // 用户组等级
    private List<String> groupUsers; // 用户组用户列表

    private int groupPoints; // 用户组积分
    private int groupEditPoints; // 用户组编辑积分
    private int groupWorkPoints; // 用户组工作积分
    private int groupTaskCreated; // 用户组创建任务数

    private int groupTaskDone; // 用户组完成任务数
    private int groupTaskGet; // 用户组领取任务数
    private int groupTaskFollowed; // 用户组关注任务数
    private int groupTaskLiked; // 用户组点赞任务数
    private int groupTaskCommented; // 用户组评论任务数

    private int groupTaskBeDone; // 用户组任务被完成数
    private int groupTaskBeGet; // 用户组任务被领取数
    private int groupTaskBeFollowed; // 用户组任务被关注数
    private int groupTaskBeLiked; // 用户组任务被点赞数
    private int groupTaskBeCommented; // 用户组任务被评论数

    public TaskUserGroup(String groupName) {
        this.groupName = groupName;
        this.groupLevel = 0;
        this.groupUsers = null;
        this.groupPoints = 0;
        this.groupEditPoints = 0;
        this.groupWorkPoints = 0;
        this.groupTaskCreated = 0;
        this.groupTaskDone = 0;
        this.groupTaskGet = 0;
        this.groupTaskFollowed = 0;
        this.groupTaskLiked = 0;
        this.groupTaskCommented = 0;
        this.groupTaskBeDone = 0;
        this.groupTaskBeGet = 0;
        this.groupTaskBeFollowed = 0;
        this.groupTaskBeLiked = 0;
        this.groupTaskBeCommented = 0;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(int groupLevel) {
        this.groupLevel = groupLevel;
    }

    public List<String> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(List<String> groupUsers) {
        this.groupUsers = groupUsers;
    }

    public int getGroupPoints() {
        return groupPoints;
    }

    public void setGroupPoints(int groupPoints) {
        this.groupPoints = groupPoints;
    }

    public int getGroupEditPoints() {
        return groupEditPoints;
    }

    public void setGroupEditPoints(int groupEditPoints) {
        this.groupEditPoints = groupEditPoints;
    }

    public int getGroupWorkPoints() {
        return groupWorkPoints;
    }

    public void setGroupWorkPoints(int groupWorkPoints) {
        this.groupWorkPoints = groupWorkPoints;
    }

    public int getGroupTaskCreated() {
        return groupTaskCreated;
    }

    public void setGroupTaskCreated(int groupTaskCreated) {
        this.groupTaskCreated = groupTaskCreated;
    }

    public int getGroupTaskDone() {
        return groupTaskDone;
    }

    public void setGroupTaskDone(int groupTaskDone) {
        this.groupTaskDone = groupTaskDone;
    }

    public int getGroupTaskGet() {
        return groupTaskGet;
    }

    public void setGroupTaskGet(int groupTaskGet) {
        this.groupTaskGet = groupTaskGet;
    }

    public int getGroupTaskFollowed() {
        return groupTaskFollowed;
    }

    public void setGroupTaskFollowed(int groupTaskFollowed) {
        this.groupTaskFollowed = groupTaskFollowed;
    }

    public int getGroupTaskLiked() {
        return groupTaskLiked;
    }

    public void setGroupTaskLiked(int groupTaskLiked) {
        this.groupTaskLiked = groupTaskLiked;
    }

    public int getGroupTaskCommented() {
        return groupTaskCommented;
    }

    public void setGroupTaskCommented(int groupTaskCommented) {
        this.groupTaskCommented = groupTaskCommented;
    }

    public int getGroupTaskBeDone() {
        return groupTaskBeDone;
    }

    public void setGroupTaskBeDone(int groupTaskBeDone) {
        this.groupTaskBeDone = groupTaskBeDone;
    }

    public int getGroupTaskBeGet() {
        return groupTaskBeGet;
    }

    public void setGroupTaskBeGet(int groupTaskBeGet) {
        this.groupTaskBeGet = groupTaskBeGet;
    }

    public int getGroupTaskBeFollowed() {
        return groupTaskBeFollowed;
    }

    public void setGroupTaskBeFollowed(int groupTaskBeFollowed) {
        this.groupTaskBeFollowed = groupTaskBeFollowed;
    }

    public int getGroupTaskBeLiked() {
        return groupTaskBeLiked;
    }

    public void setGroupTaskBeLiked(int groupTaskBeLiked) {
        this.groupTaskBeLiked = groupTaskBeLiked;
    }

    public int getGroupTaskBeCommented() {
        return groupTaskBeCommented;
    }

    public void setGroupTaskBeCommented(int groupTaskBeCommented) {
        this.groupTaskBeCommented = groupTaskBeCommented;
    }
}
