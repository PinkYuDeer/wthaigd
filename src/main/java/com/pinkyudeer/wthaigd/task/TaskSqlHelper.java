package com.pinkyudeer.wthaigd.task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.reflections.Reflections;

import com.pinkyudeer.wthaigd.annotation.FieldCheck;
import com.pinkyudeer.wthaigd.annotation.Reference;
import com.pinkyudeer.wthaigd.annotation.Table;
import com.pinkyudeer.wthaigd.task.entity.Player;
import com.pinkyudeer.wthaigd.task.entity.Tag;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.entity.record.Notification;
import com.pinkyudeer.wthaigd.task.entity.record.PlayerInteraction;
import com.pinkyudeer.wthaigd.task.entity.record.StatusChangeRecord;
import com.pinkyudeer.wthaigd.task.entity.record.TaskInteraction;
import com.pinkyudeer.wthaigd.task.entity.record.TeamMember;
import com.pinkyudeer.wthaigd.task.entity.record.TeamRequest;

public class TaskSqlHelper {

    public static class init {

        // database初始化
        // language=SQLite
        static final String INIT_DATABASE = """
                -- 核心性能优化
                PRAGMA journal_mode = WAL;                  -- 使用Write-Ahead Logging提升并发性能
                PRAGMA synchronous = NORMAL;                -- 平衡数据安全与性能
                PRAGMA cache_size = 10000;                 -- 设置10MB内存缓存（根据实际内存调整）
                PRAGMA page_size = 4096;                    -- 对齐操作系统页大小

                -- 并发控制
                PRAGMA busy_timeout = 5000;                 -- 设置5秒锁等待超时
                PRAGMA foreign_keys = ON;                   -- 强制外键约束
                PRAGMA recursive_triggers = ON;             -- 启用递归触发器

                -- 数据安全
                PRAGMA auto_vacuum = INCREMENTAL;           -- 增量自动清理空间碎片
                PRAGMA wal_autocheckpoint = 100;            -- 每100页自动执行WAL检查点
                PRAGMA secure_delete = OFF;                 -- 关闭安全删除以提升性能

                -- 内存优化
                PRAGMA temp_store = MEMORY;                 -- 临时表存储在内存中
                PRAGMA mmap_size = 268435456;               -- 分配256MB内存映射

                -- 维护配置
                PRAGMA automatic_index = ON;                -- 自动创建临时索引
                PRAGMA optimize;                            -- 自动优化查询计划
            """;

        // 玩家表
        // language=SQLite
        static final String CREATE_PLAYER_TABLE = """
                CREATE TABLE IF NOT EXISTS players (
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    player_name TEXT NOT NULL,
                    display_name TEXT CHECK(length(display_name) <= 100),
                    points INTEGER DEFAULT 0 CHECK(points >= 0),
                    level INTEGER DEFAULT 1 CHECK(level >= 1),
                    editor_points INTEGER DEFAULT 0 CHECK(editor_points >= 0),
                    work_points INTEGER DEFAULT 0 CHECK(work_points >= 0),
                    following_count INTEGER DEFAULT 0 CHECK(following_count >= 0),
                    follower_count INTEGER DEFAULT 0 CHECK(follower_count >= 0),
                    received_likes INTEGER DEFAULT 0 CHECK(received_likes >= 0),
                    given_likes INTEGER DEFAULT 0 CHECK(given_likes >= 0),
                    comments_count INTEGER DEFAULT 0 CHECK(comments_count >= 0),
                    completed_tasks INTEGER DEFAULT 0 CHECK(completed_tasks >= 0),
                    assigned_tasks INTEGER DEFAULT 0 CHECK(assigned_tasks >= 0),
                    followed_tasks INTEGER DEFAULT 0 CHECK(followed_tasks >= 0),
                    be_followed_tasks INTEGER DEFAULT 0 CHECK(be_followed_tasks >= 0),
                    total_task_duration INTEGER DEFAULT 0 CHECK(total_task_duration >= 0),
                    role TEXT NOT NULL CHECK(role IN ('SUPER_ADMIN','TEAM_ADMIN','MEMBER','GUEST')),
                    status TEXT NOT NULL CHECK(status IN ('ACTIVE','SUSPENDED','DELETED')),
                    last_login_time TEXT NOT NULL,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
                );

                CREATE INDEX IF NOT EXISTS idx_players_role_status ON players(role, status);
                CREATE INDEX IF NOT EXISTS idx_players_last_login ON players(last_login_time);
                CREATE INDEX IF NOT EXISTS idx_players_create_time ON players(create_time);
            """;

        // 团队表
        // language=SQLite
        static final String CREATE_TEAM_TABLE = """
                CREATE TABLE IF NOT EXISTS teams (
                    -- 核心字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    name TEXT NOT NULL CHECK(length(name) BETWEEN 3 AND 50),
                    description TEXT CHECK(length(description) <= 500),
                    team_code TEXT NOT NULL UNIQUE CHECK(length(team_code) = 13),
                    owner_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE,

                    -- 统计字段
                    total_members INTEGER DEFAULT 0 CHECK(total_members >= 0),
                    total_tasks INTEGER DEFAULT 0 CHECK(total_tasks >= 0),
                    total_tags INTEGER DEFAULT 0 CHECK(total_tags >= 0),
                    default_task_visibility TEXT NOT NULL CHECK(default_task_visibility IN (
                        'PUBLIC','TEAM','PRIVATE'
                    )),

                    -- 主动任务统计
                    task_done INTEGER DEFAULT 0 CHECK(task_done >= 0),
                    task_get INTEGER DEFAULT 0 CHECK(task_get >= 0),
                    task_followed INTEGER DEFAULT 0 CHECK(task_followed >= 0),
                    task_liked INTEGER DEFAULT 0 CHECK(task_liked >= 0),
                    task_commented INTEGER DEFAULT 0 CHECK(task_commented >= 0),

                    -- 被动任务统计
                    task_be_done INTEGER DEFAULT 0 CHECK(task_be_done >= 0),
                    task_be_get INTEGER DEFAULT 0 CHECK(task_be_get >= 0),
                    task_be_followed INTEGER DEFAULT 0 CHECK(task_be_followed >= 0),
                    task_be_liked INTEGER DEFAULT 0 CHECK(task_be_liked >= 0),
                    task_be_commented INTEGER DEFAULT 0 CHECK(task_be_commented >= 0),

                    -- 积分系统
                    reward_points BIGINT DEFAULT 0 CHECK(reward_points >= 0),
                    edit_points BIGINT DEFAULT 0 CHECK(edit_points >= 0),
                    work_points BIGINT DEFAULT 0 CHECK(work_points >= 0),
                    level INTEGER DEFAULT 1 CHECK(level >= 0),

                    -- 时间字段（ISO8601格式）
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    disband_time TIMESTAMP,

                    -- 权限设置（拆分为独立字段）
                    allow_member_create_task BOOLEAN DEFAULT false,
                    allow_member_assign_task BOOLEAN DEFAULT false,
                    min_assign_priority TEXT CHECK(min_assign_priority IN (
                        'CRITICAL','P1','P2','P3','P4','P5','P6','P7','P8','P9','UNDEFINED'
                    )),
                    max_task_duration INTEGER CHECK(max_task_duration >= 0),

                    -- 请求统计
                    join_requests INTEGER DEFAULT 0 CHECK(join_requests >= 0),
                    invitations INTEGER DEFAULT 0 CHECK(invitations >= 0)
                );

                -- 索引
                CREATE INDEX IF NOT EXISTS idx_teams_owner ON teams(owner_id);
                CREATE INDEX IF NOT EXISTS idx_teams_code ON teams(team_code);
                CREATE INDEX IF NOT EXISTS idx_teams_level ON teams(level);
                CREATE INDEX IF NOT EXISTS idx_teams_update_time ON teams(update_time);
                CREATE INDEX IF NOT EXISTS idx_teams_status_time ON teams(default_task_visibility, update_time);
            """;

        // 标签表
        // language=SQLite
        static final String CREATE_TAG_TABLE = """
                CREATE TABLE IF NOT EXISTS tags (
                    -- 核心属性
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    name TEXT NOT NULL CHECK(length(name) BETWEEN 1 AND 50),
                    description TEXT CHECK(length(description) <= 500),
                    color_code TEXT NOT NULL DEFAULT '#FFFFFF'
                        CHECK(color_code GLOB '#[0-9a-fA-F]{6}'),
                    font_color_code TEXT NOT NULL DEFAULT '#000000'
                        CHECK(font_color_code GLOB '#[0-9a-fA-F]{6}'),
                    is_default BOOLEAN NOT NULL DEFAULT FALSE,

                    -- 关联统计（触发器维护）
                    linked_task_count INTEGER DEFAULT 0 CHECK(linked_task_count >= 0),
                    linked_team_count INTEGER DEFAULT 0 CHECK(linked_team_count >= 0),
                    linked_player_count INTEGER DEFAULT 0 CHECK(linked_player_count >= 0),

                    -- 权限控制
                    scope TEXT NOT NULL CHECK(scope IN ('SYSTEM','PUBLIC','TEAM','PRIVATE')),
                    owner_id TEXT REFERENCES players(id) ON DELETE SET NULL,

                    -- 时间戳（ISO8601格式）
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                    -- 唯一性约束
                    CONSTRAINT unique_tag_name_per_owner UNIQUE (name, owner_id),

                    -- 外键约束
                    FOREIGN KEY(owner_id) REFERENCES players(id) ON DELETE CASCADE
                );

                -- 索引
                CREATE INDEX IF NOT EXISTS idx_tags_scope_owner ON tags(scope, owner_id);
                CREATE INDEX IF NOT EXISTS idx_tags_create_time ON tags(create_time);
                CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name COLLATE NOCASE);
            """;

        // 任务主表
        // language=SQLite
        static final String CREATE_TASK_TABLE = """
                CREATE TABLE IF NOT EXISTS tasks (
                    -- 核心字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    title TEXT NOT NULL CHECK(length(title) <= 200),
                    description TEXT NOT NULL,
                    version INTEGER DEFAULT 0 CHECK(version >= 0),

                    -- 关联字段
                    creator TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
                    team_id TEXT REFERENCES teams(id) ON DELETE SET NULL,
                    last_operator TEXT REFERENCES players(id),

                    -- 状态字段
                    priority TEXT NOT NULL CHECK(priority IN (
                        'CRITICAL','P1','P2','P3','P4','P5','P6','P7','P8','P9','UNDEFINED'
                    )),
                    importance TEXT NOT NULL CHECK(importance IN (
                        'UNDEFINED','LOW','MEDIUM','HIGH','CRITICAL'
                    )),
                    urgency TEXT NOT NULL CHECK(urgency IN (
                        'UNDEFINED','LOW','MEDIUM','HIGH','CRITICAL'
                    )),
                    status TEXT NOT NULL CHECK(status IN (
                        'UnClaimed','Blocked','UnStarted','InProgress','InTrialRun',
                        'Completed','Canceled','Closed','Rejected','Postponed','Defect'
                    )),
                    visibility TEXT NOT NULL CHECK(visibility IN ('PUBLIC','TEAM','PRIVATE')),
                    task_type TEXT CHECK(task_type IN (
                        'UNDEFINED','MAINLINE','BRANCH','DAILY','MAGIC',
                        'EVENT','ACHIEVEMENT','CHALLENGE','QUEST_FINISH'
                    )),

                    -- 时间字段（存储为ISO8601字符串）
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    start_time TIMESTAMP,
                    end_time TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    deadline TIMESTAMP,
                    reminder_time TIMESTAMP,

                    -- 持续时间字段（存储秒数）
                    estimated_duration INTEGER,
                    actual_duration INTEGER,

                    -- 重复规则
                    recurrence_rule TEXT,
                    repeat_time TIMESTAMP,
                    repeat_interval INTEGER,  -- 存储秒数
                    repeat_end_time TIMESTAMP,
                    repeat_count INTEGER CHECK(repeat_count >= 0),

                    -- 统计字段
                    assignee_count INTEGER CHECK(assignee_count >= 0),
                    follower_count INTEGER DEFAULT 0 CHECK(follower_count >= 0),
                    like_count INTEGER DEFAULT 0 CHECK(like_count >= 0),
                    comment_count INTEGER DEFAULT 0 CHECK(comment_count >= 0)
                );

                -- 索引
                CREATE INDEX IF NOT EXISTS idx_tasks_creator ON tasks(creator);
                CREATE INDEX IF NOT EXISTS idx_tasks_team ON tasks(team_id);
                CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
                CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
                CREATE INDEX IF NOT EXISTS idx_tasks_deadline ON tasks(deadline);
                CREATE INDEX IF NOT EXISTS idx_tasks_status_priority ON tasks(status, priority);
            """;

        // 通知记录表
        // language=SQLite
        static final String CREATE_NOTIFICATION_TABLE = """
                CREATE TABLE IF NOT EXISTS notifications (
                    -- 核心字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    type TEXT NOT NULL CHECK(type IN (
                        'TASK_ASSIGNED','STATUS_CHANGE','TEAM_INVITATION','TASK_REMINDER',
                        'SYSTEM_ALERT','SOCIAL_INTERACTION','APPROVAL_REQUEST','POINTS_UPDATE'
                    )),
                    title TEXT NOT NULL CHECK(length(title) <= 100),
                    content TEXT CHECK(length(content) <= 1000),
                    status TEXT NOT NULL CHECK(status IN (
                        'UNREAD','READ','ARCHIVED','PROCESSED','EXPIRED','DELETED'
                    )) DEFAULT 'UNREAD',

                    -- 关联字段
                    receiver_id TEXT NOT NULL,  -- 接收者ID（Player/Team）
                    trigger_player_id TEXT REFERENCES players(id) ON DELETE SET NULL,
                    related_task_id TEXT REFERENCES tasks(id) ON DELETE SET NULL,
                    related_team_id TEXT REFERENCES teams(id) ON DELETE SET NULL,
                    related_record_id TEXT,  -- 通用记录ID（根据entity_type判断）

                    -- 时间字段（ISO8601格式）
                    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    expire_time TIMESTAMP CHECK(expire_time > created_time),
                    read_time TIMESTAMP CHECK(read_time >= created_time),

                    -- 元数据字段
                    source_type TEXT NOT NULL CHECK(source_type IN ('SYSTEM','PLAYER','TEAM')),
                    priority TEXT CHECK(priority IN ('EMERGENCY','IMPORTANT','NORMAL','LOW')) DEFAULT 'NORMAL',
                    action_type TEXT CHECK(length(action_type) <= 50),
                    jump_link TEXT CHECK(length(jump_link) <= 200),
                    entity_type TEXT CHECK(entity_type IN (
                        'TASK','TEAM','PLAYER','TAG','RECORD','COMMENT','DEFAULT'
                    )) DEFAULT 'DEFAULT',
                    category_tag TEXT CHECK(length(category_tag) <= 30)
                );

                CREATE INDEX IF NOT EXISTS idx_notifications_receiver_status ON notifications(receiver_id, status);
                CREATE INDEX IF NOT EXISTS idx_notifications_created_time ON notifications(created_time DESC);
                CREATE INDEX IF NOT EXISTS idx_notifications_expire_status ON notifications(expire_time, status);
                CREATE INDEX IF NOT EXISTS idx_notifications_source_priority ON notifications(source_type, priority);
            """;

        // 标签关联表（多对多关系）
        // language=SQLite
        static final String CREATE_TAG_LINK_TABLE = """
                CREATE TABLE IF NOT EXISTS tag_links (
                    -- 核心关联字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    tag_id TEXT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
                    entity_type TEXT NOT NULL CHECK(entity_type IN (
                        'TASK','TEAM','PLAYER','TAG','COMMENT','DEFAULT'
                    )),
                    entity_id TEXT NOT NULL CHECK(length(entity_id) = 36),

                    -- 时间管理
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                    -- 权限控制
                    visibility TEXT NOT NULL CHECK(visibility IN (
                        'PUBLIC','TEAM','PRIVATE'
                    )) DEFAULT 'TEAM',
                    source_type TEXT NOT NULL CHECK(source_type IN (
                        'SYSTEM','PLAYER','TEAM'
                    )),

                    -- 状态管理
                    is_active BOOLEAN NOT NULL DEFAULT TRUE,

                    -- 扩展字段
                    metadata TEXT CHECK(length(metadata) <= 2000),

                    -- 唯一性约束
                    CONSTRAINT unique_tag_entity UNIQUE (tag_id, entity_type, entity_id),

                    -- 外键约束
                    FOREIGN KEY(tag_id) REFERENCES tags(id) ON UPDATE CASCADE
                );

                CREATE INDEX IF NOT EXISTS idx_tag_links_entity ON tag_links(tag_id, entity_type);
                CREATE INDEX IF NOT EXISTS idx_tag_links_entity_id ON tag_links(entity_id);
                CREATE INDEX IF NOT EXISTS idx_tag_links_create_time ON tag_links(create_time DESC);
            """;

        // 团队请求表
        // language=SQLite
        static final String CREATE_TEAM_REQUEST_TABLE = """
                CREATE TABLE IF NOT EXISTS team_requests (
                    -- 核心字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    request_type TEXT NOT NULL CHECK(request_type IN ('JOIN','INVITE')),
                    status TEXT NOT NULL CHECK(status IN ('PENDING','APPROVED','REJECTED','EXPIRED','CANCELLED')) DEFAULT 'PENDING',

                    -- 关联字段
                    team_id TEXT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                    applicant_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
                    inviter_id TEXT REFERENCES players(id) ON DELETE SET NULL,
                    handler_id TEXT REFERENCES players(id) ON DELETE SET NULL,

                    -- 内容字段
                    reason TEXT CHECK(length(reason) <= 500),
                    handle_reason TEXT CHECK(length(handle_reason) <= 500),
                    metadata TEXT CHECK(length(metadata) <= 2000),

                    -- 时间字段（ISO8601格式）
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    expire_time TIMESTAMP CHECK(expire_time > create_time),
                    handle_time TIMESTAMP CHECK(handle_time >= create_time),

                    -- 来源追踪
                    source_type TEXT NOT NULL CHECK(source_type IN ('SYSTEM','PLAYER','TEAM')),

                    -- 外键约束
                    FOREIGN KEY(team_id) REFERENCES teams(id) ON UPDATE CASCADE,
                    FOREIGN KEY(applicant_id) REFERENCES players(id) ON UPDATE CASCADE,
                    FOREIGN KEY(inviter_id) REFERENCES players(id) ON UPDATE CASCADE,
                    FOREIGN KEY(handler_id) REFERENCES players(id) ON UPDATE CASCADE
                );

                CREATE INDEX IF NOT EXISTS idx_team_requests_team_status ON team_requests(team_id, status);
                CREATE INDEX IF NOT EXISTS idx_team_requests_applicant ON team_requests(applicant_id);
                CREATE INDEX IF NOT EXISTS idx_team_requests_expire ON team_requests(expire_time);
                CREATE INDEX IF NOT EXISTS idx_team_requests_composite ON team_requests(request_type, source_type, status);
            """;

        // 团队成员表
        // language=SQLite
        static final String CREATE_TEAM_MEMBER_TABLE = """
                CREATE TABLE IF NOT EXISTS team_members (
                    -- 核心关联字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    team_id TEXT NOT NULL REFERENCES teams(id) ON DELETE CASCADE ON UPDATE CASCADE,
                    player_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE ON UPDATE CASCADE,

                    -- 成员属性
                    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN','MEMBER','GUEST')),
                    status TEXT NOT NULL CHECK(status IN ('ACTIVE','SUSPENDED','LEFT')) DEFAULT 'ACTIVE',

                    -- 贡献统计（需要触发器维护）
                    completed_tasks INTEGER DEFAULT 0 CHECK(completed_tasks >= 0),
                    contribution_points BIGINT DEFAULT 0 CHECK(contribution_points >= 0),
                    total_duration BIGINT DEFAULT 0 CHECK(total_duration >= 0), -- 存储秒数

                    -- 操作记录
                    last_operation_time TIMESTAMP CHECK(last_operation_time >= join_time),
                    last_operator_id TEXT REFERENCES players(id) ON DELETE SET NULL,

                    -- 时间戳（与BaseRecord保持一致）
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                    -- 唯一性约束（防止重复加入）
                    CONSTRAINT unique_team_player UNIQUE (team_id, player_id),

                    -- 外键约束
                    FOREIGN KEY(team_id) REFERENCES teams(id) ON UPDATE CASCADE,
                    FOREIGN KEY(player_id) REFERENCES players(id) ON UPDATE CASCADE
                );

                CREATE INDEX IF NOT EXISTS idx_team_members_team_player ON team_members(team_id, player_id);
                CREATE INDEX IF NOT EXISTS idx_team_members_role_status ON team_members(role, status);
                CREATE INDEX IF NOT EXISTS idx_team_members_join_time ON team_members(join_time DESC);
                CREATE INDEX IF NOT EXISTS idx_team_members_last_operation ON team_members(last_operation_time DESC);
            """;

        // 玩家互动表
        // language=SQLite
        static final String CREATE_PLAYER_INTERACTION_TABLE = """
                CREATE TABLE IF NOT EXISTS player_interactions (
                    -- 核心字段
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    type TEXT NOT NULL CHECK(type IN (
                        'FOLLOW','UNFOLLOW','LIKE_PROFILE','COMMENT_PROFILE',
                        'PRIVATE_MESSAGE','MENTION','GIFT_SEND','FRIEND_REQUEST','REPORT'
                    )),
                    status TEXT NOT NULL CHECK(status IN (
                        'ACTIVE','DELETED','REVOKED','ARCHIVED'
                    )) DEFAULT 'ACTIVE',
                    visibility TEXT NOT NULL CHECK(visibility IN (
                        'PUBLIC','TEAM','FRIENDS','PRIVATE'
                    )) DEFAULT 'FRIENDS',

                    -- 关联字段
                    initiator_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
                    receiver_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
                    related_task_id TEXT REFERENCES tasks(id) ON DELETE SET NULL,
                    related_interaction_id TEXT REFERENCES player_interactions(id) ON DELETE SET NULL,

                    -- 内容字段
                    content TEXT CHECK(length(content) <= 1000),
                    metadata TEXT CHECK(length(metadata) <= 2000),

                    -- 时间字段（ISO8601格式）
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    reminder_time TIMESTAMP CHECK(reminder_time > create_time),

                    -- 操作追踪
                    operator_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE,

                    -- 唯一性约束（防止重复互动）
                    CONSTRAINT unique_interaction UNIQUE (type, initiator_id, receiver_id, related_task_id)
                );

                CREATE INDEX IF NOT EXISTS idx_interaction_initiator_receiver ON player_interactions(initiator_id, receiver_id);
                CREATE INDEX IF NOT EXISTS idx_interaction_type_status ON player_interactions(type, status);
                CREATE INDEX IF NOT EXISTS idx_interaction_create_time ON player_interactions(create_time DESC);
                CREATE INDEX IF NOT EXISTS idx_interaction_visibility ON player_interactions(visibility);
            """;

        // 任务互动表
        // language=SQLite
        static final String CREATE_TASK_INTERACTION_TABLE = """
                CREATE TABLE IF NOT EXISTS task_interactions (
                    -- 基础记录字段（继承BaseRecord）
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    operator_id TEXT NOT NULL REFERENCES players(id) ON UPDATE CASCADE,

                    -- 核心关联字段
                    type TEXT NOT NULL CHECK(type IN (
                        'CLAIM','ASSIGN','FOLLOW','UNFOLLOW','LIKE','UNLIKE',
                        'COMMENT','REMINDER_SET','REMINDER_CANCEL','PROGRESS_UPDATE',
                        'SHARE','REPORT','COLLABORATE','REQUEST_HELP','ACCEPT',
                        'REJECT','REQUEST_DELAY','LINK_SUBTASK','UNLINK_SUBTASK','RATE','REWARD_CLAIM'
                    )),
                    task_id TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE ON UPDATE CASCADE,
                    player_id TEXT NOT NULL REFERENCES players(id) ON DELETE CASCADE ON UPDATE CASCADE,

                    -- 关联上下文
                    assigner_count INTEGER CHECK(assigner_count >= 0),
                    comment_id TEXT REFERENCES task_interactions(id) ON DELETE SET NULL,
                    parent_task_id TEXT REFERENCES tasks(id) ON DELETE SET NULL,

                    -- 互动内容
                    content TEXT CHECK(length(content) <= 1000),
                    status TEXT NOT NULL CHECK(status IN (
                        'PENDING','ACTIVE','DELETED','REVOKED','ARCHIVED','EXPIRED'
                    )) DEFAULT 'ACTIVE',

                    -- 时间控制
                    reminder_time TIMESTAMP CHECK(reminder_time > create_time),

                    -- 进度跟踪
                    progress_percentage INTEGER CHECK(progress_percentage BETWEEN 0 AND 100),

                    -- 扩展信息
                    metadata TEXT CHECK(length(metadata) <= 2000),

                    -- 外键约束
                    FOREIGN KEY(parent_task_id) REFERENCES tasks(id) ON UPDATE CASCADE,
                    FOREIGN KEY(comment_id) REFERENCES task_interactions(id) ON UPDATE CASCADE
                );

                CREATE INDEX IF NOT EXISTS idx_task_interactions_composite ON task_interactions(task_id, type, status);
                CREATE INDEX IF NOT EXISTS idx_task_interactions_player ON task_interactions(player_id);
                CREATE INDEX IF NOT EXISTS idx_task_interactions_reminder ON task_interactions(reminder_time DESC);
                CREATE INDEX IF NOT EXISTS idx_task_interactions_progress ON task_interactions(progress_percentage);
            """;

        // 任务状态变更历史表
        // language=SQLite
        static final String CREATE_TASK_HISTORY_TABLE = """
                CREATE TABLE IF NOT EXISTS task_histories (
                    -- 基础记录字段（继承BaseRecord）
                    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    operator_id TEXT NOT NULL REFERENCES players(id) ON UPDATE CASCADE,

                    -- 核心状态变更信息
                    task_id TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                    old_status TEXT NOT NULL CHECK(old_status IN (
                        'DRAFT','PENDING','ACTIVE','IN_PROGRESS',
                        'BLOCKED','COMPLETED','ARCHIVED','DELETED'
                    )),
                    new_status TEXT NOT NULL CHECK(new_status IN (
                        'DRAFT','PENDING','ACTIVE','IN_PROGRESS',
                        'BLOCKED','COMPLETED','ARCHIVED','DELETED'
                    )),

                    -- 变更上下文
                    reason TEXT CHECK(length(reason) <= 500),
                    is_automatic BOOLEAN NOT NULL DEFAULT FALSE,
                    related_team_id TEXT REFERENCES teams(id) ON DELETE SET NULL,

                    -- 来源追踪
                    source_type TEXT NOT NULL CHECK(source_type IN ('SYSTEM','PLAYER','TEAM')) DEFAULT 'SYSTEM',

                    -- 扩展信息
                    metadata TEXT CHECK(length(metadata) <= 2000),

                    -- 外键约束
                    FOREIGN KEY(operator_id) REFERENCES players(id) ON UPDATE CASCADE,
                    FOREIGN KEY(related_team_id) REFERENCES teams(id) ON UPDATE CASCADE
                );

                CREATE INDEX IF NOT EXISTS idx_task_history_composite ON task_histories(task_id, create_time DESC);
                CREATE INDEX IF NOT EXISTS idx_status_transition ON task_histories(old_status, new_status);
                CREATE INDEX IF NOT EXISTS idx_operator_history ON task_histories(operator_id, create_time DESC);
            """;

        // 按顺序创建一个sql String数组
        public static final String[] CREATE_TABLES = { INIT_DATABASE, CREATE_PLAYER_TABLE, CREATE_TEAM_TABLE,
            CREATE_TAG_TABLE, CREATE_TASK_TABLE, CREATE_NOTIFICATION_TABLE, CREATE_TAG_LINK_TABLE,
            CREATE_TEAM_REQUEST_TABLE, CREATE_TEAM_MEMBER_TABLE, CREATE_PLAYER_INTERACTION_TABLE,
            CREATE_TASK_INTERACTION_TABLE, CREATE_TASK_HISTORY_TABLE };

        // 根据注解生成相应的 SQLite CHECK 约束
        public static String generateCheckConstraint(Field field) {
            // 获取javax.annotation.Nonnull注解
            if (field.getAnnotation(javax.annotation.Nonnull.class) != null) {
                return "NOT NULL";
            }

            // 获取 FieldCheck 注解
            FieldCheck fieldCheck = field.getAnnotation(FieldCheck.class);
            if (fieldCheck == null) return "";

            StringBuilder checkConstraint = new StringBuilder();

            // 针对不同类型的校验
            switch (fieldCheck.type()) {
                case NOT_VALUE -> checkConstraint.append("CHECK(")
                    .append(field.getName())
                    .append(" != ")
                    .append(fieldCheck.notValue())
                    .append(")");
                case MIN -> checkConstraint.append("CHECK(")
                    .append(field.getName())
                    .append(" >= ")
                    .append(fieldCheck.min())
                    .append(")");
                case MAX -> checkConstraint.append("CHECK(")
                    .append(field.getName())
                    .append(" <= ")
                    .append(fieldCheck.max())
                    .append(")");
                case RANGE -> checkConstraint.append("CHECK(")
                    .append(field.getName())
                    .append(" BETWEEN ")
                    .append(fieldCheck.min())
                    .append(" AND ")
                    .append(fieldCheck.max())
                    .append(")");
                case REGEX -> checkConstraint.append("CHECK(")
                    .append(field.getName())
                    .append(" REGEXP '")
                    .append(fieldCheck.regex())
                    .append("')");
                case LENGTH -> checkConstraint.append("CHECK(LENGTH(")
                    .append(field.getName())
                    .append(") >= ")
                    .append(fieldCheck.min())
                    .append(" AND LENGTH(")
                    .append(field.getName())
                    .append(") <= ")
                    .append(fieldCheck.max())
                    .append(")");
                case UUID -> checkConstraint.append("CHECK(LENGTH(")
                    .append(field.getName())
                    .append(") = 36)");
                case ENUM -> {
                    if (fieldCheck.enumClass() != Enum.class) {
                        // 对于枚举类型，可以检查是否在指定的值中
                        checkConstraint.append("CHECK(")
                            .append(field.getName())
                            .append(" IN (")
                            .append(getEnumValues(fieldCheck.enumClass()))
                            .append("))");
                    }
                }

            }

            return checkConstraint.toString();
        }

        // 约束
        private static String generateIdReferences(Field field) {
            Reference reference = field.getAnnotation(Reference.class);
            if (reference == null) return null;

            StringBuilder references = new StringBuilder();
            references.append("FOREIGN KEY(")
                .append(field.getName())
                .append(") REFERENCES ");

            Reference.Type ref_type = reference.referenceType();
            switch (ref_type) {
                case PLAYER -> references.append(
                    Player.class.getAnnotation(Table.class)
                        .name());
                case TEAM -> references.append(
                    Team.class.getAnnotation(Table.class)
                        .name());
                case TASK -> references.append(
                    Task.class.getAnnotation(Table.class)
                        .name());
                case TAG -> references.append(
                    Tag.class.getAnnotation(Table.class)
                        .name());
                case NOTIFICATION -> references.append(
                    Notification.class.getAnnotation(Table.class)
                        .name());
                case TEAM_REQUEST -> references.append(
                    TeamRequest.class.getAnnotation(Table.class)
                        .name());
                case TEAM_MEMBER -> references.append(
                    TeamMember.class.getAnnotation(Table.class)
                        .name());
                case PLAYER_INTERACTION -> references.append(
                    PlayerInteraction.class.getAnnotation(Table.class)
                        .name());
                case TASK_INTERACTION -> references.append(
                    TaskInteraction.class.getAnnotation(Table.class)
                        .name());
                case TASK_HISTORY -> references.append(
                    StatusChangeRecord.class.getAnnotation(Table.class)
                        .name());
                default -> throw new IllegalArgumentException("Unsupported reference type: " + ref_type);
            }
            references.append("(id) ON DELETE CASCADE ON UPDATE CASCADE;");
            return references.toString();
        }

        // 获取枚举类的值
        private static String getEnumValues(Class<?> enumClass) {
            if (!enumClass.isEnum()) {
                throw new IllegalArgumentException("Provided class is not an Enum");
            }

            StringBuilder values = new StringBuilder();
            for (Object enumConstant : enumClass.getEnumConstants()) {
                values.append("'")
                    .append(((Enum<?>) enumConstant).name())
                    .append("', ");
            }

            // 去掉最后的逗号和空格
            return !(values.length() == 0) ? values.substring(0, values.length() - 2) : "";
        }

        // 生成 CREATE TABLE SQL
        public static String generateCreateTableSql(Class<?> clazz) {
            Table table = clazz.getAnnotation(Table.class);
            if (table == null) {
                return null;
            }
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            // 根据clazz上的 @Table 注解获取name当做表名
            sql.append(
                clazz.getAnnotation(Table.class)
                    .name())
                .append(" (");

            List<String> columnDefinitions = new ArrayList<>();
            List<Field> refer = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                StringBuilder columnDefinition = new StringBuilder();
                columnDefinition.append(field.getName())
                    .append(" ");

                // 根据字段类型生成 SQLite 数据类型
                if (field.getType() == String.class) {
                    columnDefinition.append("TEXT");
                } else if (field.getType() == int.class || field.getType() == Integer.class) {
                    columnDefinition.append("INTEGER");
                } else if (field.getType() == long.class || field.getType() == Long.class) {
                    columnDefinition.append("BIGINT");
                } else if (field.getType() == double.class || field.getType() == Double.class) {
                    columnDefinition.append("REAL");
                } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    columnDefinition.append("BOOLEAN");
                } else if (field.getType() == java.util.Date.class) {
                    columnDefinition.append("TIMESTAMP");
                } else if (field.getType() == UUID.class) {
                    columnDefinition.append("TEXT");

                } else {
                    throw new IllegalArgumentException("Unsupported field type: " + field.getType());
                }

                // 获取字段的检查约束
                String checkConstraint = generateCheckConstraint(field);
                if (!checkConstraint.isEmpty()) {
                    columnDefinition.append(" ")
                        .append(checkConstraint);
                }
                columnDefinitions.add(columnDefinition.toString());

                // 检查字段是否有外键约束
                if (generateIdReferences(field) != null) {
                    refer.add(field);
                }
            }

            sql.append(String.join(", ", columnDefinitions));
            sql.append(");");

            // 添加外键约束
            for (Field field : refer) {
                sql.append(generateIdReferences(field));
            }

            return sql.toString();
        }

        // 扫描task.entity包下的所有类generateCreateTableSql
        public static List<String> generateAllCreateTableSql() {
            // 使用 Reflections 库扫描指定包
            Reflections reflections = new Reflections("com.pinkyudeer.wthaigd.task.entity");
            Set<Class<?>> annotatedClasses = reflections.getSubTypesOf(Object.class);
            List<String> createTableSqlList = new ArrayList<>();
            for (Class<?> clazz : annotatedClasses) {
                String createTableSql = generateCreateTableSql(clazz);
                if (createTableSql != null) {
                    createTableSqlList.add(createTableSql);
                }
            }
            return createTableSqlList;
        }
    }

    public static class add {

    }

    public static class delete {
    }

    public static class update {
    }

    public static class select {
    }
}
