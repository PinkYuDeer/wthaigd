package com.pinkyudeer.wthaigd.task.entity;

import java.util.List;

public class TaskTeam {

    public String teamName; // 用户组名
    public int teamLevel; // 用户组等级
    public List<String> teamUsers; // 用户组用户列表

    public int teamPoints; // 用户组积分
    public int teamEditPoints; // 用户组编辑积分
    public int teamWorkPoints; // 用户组工作积分
    public int teamTaskCreated; // 用户组创建任务数

    public int teamTaskDone; // 用户组完成任务数
    public int teamTaskGet; // 用户组领取任务数
    public int teamTaskFollowed; // 用户组关注任务数
    public int teamTaskLiked; // 用户组点赞任务数
    public int teamTaskCommented; // 用户组评论任务数

    public int teamTaskBeDone; // 用户组任务被完成数
    public int teamTaskBeGet; // 用户组任务被领取数
    public int teamTaskBeFollowed; // 用户组任务被关注数
    public int teamTaskBeLiked; // 用户组任务被点赞数
    public int teamTaskBeCommented; // 用户组任务被评论数

    public TaskTeam(String teamName) {
        this.teamName = teamName;
        this.teamLevel = 0;
        this.teamUsers = null;
        this.teamPoints = 0;
        this.teamEditPoints = 0;
        this.teamWorkPoints = 0;
        this.teamTaskCreated = 0;
        this.teamTaskDone = 0;
        this.teamTaskGet = 0;
        this.teamTaskFollowed = 0;
        this.teamTaskLiked = 0;
        this.teamTaskCommented = 0;
        this.teamTaskBeDone = 0;
        this.teamTaskBeGet = 0;
        this.teamTaskBeFollowed = 0;
        this.teamTaskBeLiked = 0;
        this.teamTaskBeCommented = 0;
    }
}
