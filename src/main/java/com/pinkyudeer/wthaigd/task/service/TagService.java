package com.pinkyudeer.wthaigd.task.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.db.EntityHandler;
import com.pinkyudeer.wthaigd.db.SQLHelper;
import com.pinkyudeer.wthaigd.db.SQLiteManager;
import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.task.dao.TagDao;
import com.pinkyudeer.wthaigd.task.dao.record.TagLinkDao;
import com.pinkyudeer.wthaigd.task.entity.Tag;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.record.Notification.RelatedEntityType;
import com.pinkyudeer.wthaigd.task.entity.record.Notification.SourceType;
import com.pinkyudeer.wthaigd.task.entity.record.TagLink;

public final class TagService {

    private TagService() {}

    public static Tag createTag(TaskService.PermissionContext context, String name, String description,
        String colorCode, Tag.TagScope scope, UUID ownerTeamId) {
        if (context == null) throw new IllegalArgumentException("权限上下文不能为空");
        if (isBlank(name)) throw new IllegalArgumentException("标签名称不能为空");

        Tag.TagScope finalScope = scope == null ? Tag.TagScope.PUBLIC : scope;
        assertCanCreateTag(context, finalScope, ownerTeamId);

        return SQLiteManager.transaction(() -> {
            Tag existing = findVisibleTagByName(context, name.trim(), finalScope, ownerTeamId);
            if (existing != null) return existing;

            Tag tag = new Tag(name.trim(), description, normalizeColor(colorCode));
            tag.setScope(finalScope);
            if (finalScope == Tag.TagScope.PRIVATE) tag.setOwnerId(context.getActorId());
            if (finalScope == Tag.TagScope.TEAM) tag.setOwnerTeamId(ownerTeamId);
            tag.setUpdateTime(LocalDateTime.now());
            Integer result = TagDao.insert(tag);
            return result != null && result > 0 ? tag : null;
        });
    }

    public static Tag getOrCreateTaskTag(TaskService.PermissionContext context, String name, String description,
        String colorCode, Tag.TagScope scope, UUID ownerTeamId) {
        Tag.TagScope finalScope = scope == null ? Tag.TagScope.PUBLIC : scope;
        Tag tag = findVisibleTagByName(context, name, finalScope, ownerTeamId);
        return tag == null ? createTag(context, name, description, colorCode, finalScope, ownerTeamId) : tag;
    }

    public static boolean addTagToTask(TaskService.PermissionContext context, String taskId, UUID tagId) {
        Task task = requireWritableTask(context, taskId);
        Tag tag = requireVisibleTag(context, tagId);
        assertTagAppliesToTask(context, tag, task);

        return SQLiteManager.transaction(() -> {
            TagLink existing = findTaskLink(taskId, tagId, false);
            if (existing == null) {
                TagLink link = new TagLink(
                    tagId,
                    RelatedEntityType.TASK,
                    UUID.fromString(taskId),
                    context.getActorId());
                link.setSourceType(SourceType.PLAYER);
                link.setVisibility(task.getVisibility() == null ? Task.PrivacyLevel.PRIVATE : task.getVisibility());
                TagLinkDao.insert(link);
            } else if (!Boolean.TRUE.equals(existing.getIsActive())) {
                TagLink oldLink = UtilHelper.deepClone(existing, TagLink.class);
                existing.setIsActive(true);
                existing.setVisibility(task.getVisibility() == null ? Task.PrivacyLevel.PRIVATE : task.getVisibility());
                existing.setSourceType(SourceType.PLAYER);
                existing.setOperatorId(context.getActorId());
                TagLinkDao.updateByIdByCompare(existing, oldLink);
            }
            updateTagLinkCount(tagId, RelatedEntityType.TASK);
            return true;
        });
    }

    public static Tag addTagToTask(TaskService.PermissionContext context, String taskId, String tagName,
        String description, String colorCode, Tag.TagScope scope) {
        Task task = requireWritableTask(context, taskId);
        Tag.TagScope finalScope = scope == null ? Tag.TagScope.PUBLIC : scope;
        UUID ownerTeamId = finalScope == Tag.TagScope.TEAM ? task.getTeamId() : context.getTeamId();
        Tag tag = getOrCreateTaskTag(context, tagName, description, colorCode, finalScope, ownerTeamId);
        if (tag == null) return null;
        return addTagToTask(context, taskId, tag.getId()) ? tag : null;
    }

    public static boolean removeTagFromTask(TaskService.PermissionContext context, String taskId, UUID tagId) {
        requireWritableTask(context, taskId);
        Tag tag = requireVisibleTag(context, tagId);

        return SQLiteManager.transaction(() -> {
            TagLink existing = findTaskLink(taskId, tagId, true);
            if (existing == null) return false;
            TagLink oldLink = UtilHelper.deepClone(existing, TagLink.class);
            existing.setIsActive(false);
            existing.setOperatorId(context.getActorId());
            TagLinkDao.updateByIdByCompare(existing, oldLink);
            updateTagLinkCount(tag.getId(), RelatedEntityType.TASK);
            return true;
        });
    }

    public static boolean setTagsForTask(TaskService.PermissionContext context, String taskId, List<UUID> tagIds) {
        requireWritableTask(context, taskId);
        List<UUID> desired = tagIds == null ? new ArrayList<>() : new ArrayList<>(tagIds);
        return SQLiteManager.transaction(() -> {
            for (UUID tagId : desired) {
                addTagToTask(context, taskId, tagId);
            }
            for (TagLink link : getActiveTaskTagLinks(taskId)) {
                if (!desired.contains(link.getTagId())) {
                    removeTagFromTask(context, taskId, link.getTagId());
                }
            }
            return true;
        });
    }

    public static List<Tag> getTagsForTask(String taskId) {
        if (isBlank(taskId)) return new ArrayList<>();
        return EntityHandler.handleList(
            SQLHelper.select(Tag.class)
                .join(TagLink.class, UtilHelper.getField(Tag.class, "id"), UtilHelper.getField(TagLink.class, "tagId"))
                .where("tag_links.entity_type", SQLHelper.Operator.EQ, RelatedEntityType.TASK)
                .where("tag_links.entity_id", SQLHelper.Operator.EQ, UUID.fromString(taskId))
                .where("tag_links.is_active", SQLHelper.Operator.EQ, 1)
                .execute(),
            Tag.class);
    }

    public static List<TagLink> getActiveTaskTagLinks(String taskId) {
        if (isBlank(taskId)) return new ArrayList<>();
        return EntityHandler.handleList(
            SQLHelper.select(TagLink.class)
                .where("entity_type", SQLHelper.Operator.EQ, RelatedEntityType.TASK)
                .where("entity_id", SQLHelper.Operator.EQ, UUID.fromString(taskId))
                .where("is_active", SQLHelper.Operator.EQ, 1)
                .execute(),
            TagLink.class);
    }

    public static List<Task> getTasksByTag(UUID tagId, UUID viewerId, boolean isOp) {
        if (tagId == null) return new ArrayList<>();
        List<TagLink> links = EntityHandler.handleList(
            SQLHelper.select(TagLink.class)
                .where("tag_id", SQLHelper.Operator.EQ, tagId)
                .where("entity_type", SQLHelper.Operator.EQ, RelatedEntityType.TASK)
                .where("is_active", SQLHelper.Operator.EQ, 1)
                .execute(),
            TagLink.class);
        List<Task> tasks = new ArrayList<>();
        for (TagLink link : links) {
            Task task = TaskService.getTask(
                link.getEntityId()
                    .toString());
            if (TaskService.canViewTask(viewerId, isOp, task)) tasks.add(task);
        }
        return tasks;
    }

    public static List<Tag> getVisibleTags(UUID playerId, boolean isOp, UUID teamId) {
        List<Tag> all = getAllTags();
        List<Tag> visible = new ArrayList<>();
        for (Tag tag : all) {
            if (canViewTag(playerId, isOp, teamId, tag)) visible.add(tag);
        }
        return visible;
    }

    public static Tag getTag(UUID tagId) {
        return tagId == null ? null : TagDao.selectById(tagId);
    }

    public static List<Tag> getAllTags() {
        try {
            return TagDao.selectAll();
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to load tags", e);
            return new ArrayList<>();
        }
    }

    private static void assertCanCreateTag(TaskService.PermissionContext context, Tag.TagScope scope,
        UUID ownerTeamId) {
        if (context.isOp()) return;
        if (scope == Tag.TagScope.SYSTEM) throw new SecurityException("只有 OP 可创建系统标签");
        if (scope == Tag.TagScope.PRIVATE || scope == Tag.TagScope.PUBLIC) return;
        if (scope == Tag.TagScope.TEAM && ownerTeamId != null
            && ownerTeamId.equals(context.getTeamId())
            && context.getTeamRole() != null) {
            return;
        }
        throw new SecurityException("无权创建团队标签");
    }

    private static Task requireWritableTask(TaskService.PermissionContext context, String taskId) {
        if (context == null) throw new IllegalArgumentException("权限上下文不能为空");
        if (isBlank(taskId)) throw new IllegalArgumentException("任务 ID 不能为空");
        Task task = TaskService.getTask(taskId);
        if (task == null) throw new IllegalArgumentException("任务不存在");
        if (!TaskService.canWriteTask(context, task)) throw new SecurityException("无权修改任务标签");
        return task;
    }

    private static Tag requireVisibleTag(TaskService.PermissionContext context, UUID tagId) {
        Tag tag = getTag(tagId);
        if (tag == null) throw new IllegalArgumentException("标签不存在");
        if (!canViewTag(context.getActorId(), context.isOp(), context.getTeamId(), tag)) {
            throw new SecurityException("无权使用此标签");
        }
        return tag;
    }

    private static void assertTagAppliesToTask(TaskService.PermissionContext context, Tag tag, Task task) {
        if (context.isOp()) return;
        if (tag.getScope() == Tag.TagScope.TEAM) {
            UUID tagTeam = tag.getOwnerTeamId();
            if (tagTeam == null || task.getTeamId() == null || !tagTeam.equals(task.getTeamId())) {
                throw new SecurityException("团队标签只能用于同团队任务");
            }
        }
        if (tag.getScope() == Tag.TagScope.PRIVATE && !context.getActorId()
            .equals(tag.getOwnerId())) {
            throw new SecurityException("私有标签只能由拥有者使用");
        }
    }

    private static boolean canViewTag(UUID playerId, boolean isOp, UUID teamId, Tag tag) {
        if (tag == null) return false;
        if (isOp || tag.getScope() == Tag.TagScope.SYSTEM || tag.getScope() == Tag.TagScope.PUBLIC) return true;
        if (tag.getScope() == Tag.TagScope.PRIVATE) return playerId != null && playerId.equals(tag.getOwnerId());
        return tag.getScope() == Tag.TagScope.TEAM && teamId != null && teamId.equals(tag.getOwnerTeamId());
    }

    private static Tag findVisibleTagByName(TaskService.PermissionContext context, String name, Tag.TagScope scope,
        UUID ownerTeamId) {
        if (context == null || isBlank(name)) return null;
        for (Tag tag : getAllTags()) {
            if (!name.trim()
                .equalsIgnoreCase(tag.getName())) continue;
            if (scope != null && tag.getScope() != scope) continue;
            if (scope == Tag.TagScope.TEAM && ownerTeamId != null && !ownerTeamId.equals(tag.getOwnerTeamId()))
                continue;
            if (canViewTag(context.getActorId(), context.isOp(), context.getTeamId(), tag)) return tag;
        }
        return null;
    }

    private static TagLink findTaskLink(String taskId, UUID tagId, boolean activeOnly) {
        List<TagLink> links = EntityHandler.handleList(
            SQLHelper.select(TagLink.class)
                .where("tag_id", SQLHelper.Operator.EQ, tagId)
                .where("entity_type", SQLHelper.Operator.EQ, RelatedEntityType.TASK)
                .where("entity_id", SQLHelper.Operator.EQ, UUID.fromString(taskId))
                .execute(),
            TagLink.class);
        for (TagLink link : links) {
            if (!activeOnly || Boolean.TRUE.equals(link.getIsActive())) return link;
        }
        return null;
    }

    private static void updateTagLinkCount(UUID tagId, RelatedEntityType entityType) {
        Tag tag = getTag(tagId);
        if (tag == null) return;
        int count = countActiveLinks(tagId, entityType);
        Tag oldTag = UtilHelper.shallowClone(tag);
        if (entityType == RelatedEntityType.TASK) tag.setLinkedTaskCount(count);
        else if (entityType == RelatedEntityType.TEAM) tag.setLinkedTeamCount(count);
        else if (entityType == RelatedEntityType.PLAYER) tag.setLinkedPlayerCount(count);
        tag.setUpdateTime(LocalDateTime.now());
        TagDao.updateByIdByCompare(tag, oldTag);
    }

    private static int countActiveLinks(UUID tagId, RelatedEntityType entityType) {
        Integer count = SQLiteManager.query(
            "SELECT COUNT(*) AS c FROM tag_links WHERE tag_id = ? AND entity_type = ? AND is_active = 1",
            rs -> rs.next() ? rs.getInt("c") : 0,
            tagId,
            entityType.name());
        return count == null ? 0 : count;
    }

    private static String normalizeColor(String colorCode) {
        if (colorCode == null || !colorCode.matches("^#[0-9a-fA-F]{6}$")) return "#FFFFFF";
        return colorCode.toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim()
            .isEmpty();
    }
}
