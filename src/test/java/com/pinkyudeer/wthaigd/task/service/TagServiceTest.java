package com.pinkyudeer.wthaigd.task.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.pinkyudeer.wthaigd.task.entity.Tag;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.test.SqliteTestSupport;

public class TagServiceTest extends SqliteTestSupport {

    @Test
    public void taskTagCanBeCreatedLinkedDeduplicatedAndRemoved() {
        UUID actor = UUID.randomUUID();
        TaskService.PermissionContext context = new TaskService.PermissionContext(actor, null, null, false);
        Task task = TaskService
            .createTask(context, "wire pump", "build line", Task.Importance.HIGH, Task.Urgency.MEDIUM);
        assertNotNull(task);

        Tag tag = TagService
            .addTagToTask(context, task.getId(), "logistics", "routing", "#336699", Tag.TagScope.PUBLIC);
        assertNotNull(tag);
        assertEquals(
            1,
            TagService.getTagsForTask(task.getId())
                .size());
        assertEquals(
            Integer.valueOf(1),
            TagService.getTag(tag.getId())
                .getLinkedTaskCount());

        assertNotNull(
            TagService.addTagToTask(context, task.getId(), "logistics", "routing", "#336699", Tag.TagScope.PUBLIC));
        assertEquals(
            1,
            TagService.getTagsForTask(task.getId())
                .size());

        assertTrue(TagService.removeTagFromTask(context, task.getId(), tag.getId()));
        assertEquals(
            0,
            TagService.getTagsForTask(task.getId())
                .size());
        assertEquals(
            Integer.valueOf(0),
            TagService.getTag(tag.getId())
                .getLinkedTaskCount());
    }

    @Test
    public void teamScopedTagCanOnlyBeUsedOnSameTeamTask() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Team team = TeamService.createLocalTeam("team-tag", owner, null);
        TaskService.PermissionContext ownerContext = TeamService.contextFor(owner, team.getId(), false);
        Task teamTask = TaskService
            .createTask(ownerContext, "shared task", "team scoped", Task.Importance.MEDIUM, Task.Urgency.LOW);
        assertNotNull(teamTask);

        Tag tag = TagService
            .addTagToTask(ownerContext, teamTask.getId(), "chemistry", null, "#00AA44", Tag.TagScope.TEAM);
        assertNotNull(tag);
        assertEquals(team.getId(), tag.getOwnerTeamId());

        TaskService.PermissionContext otherContext = new TaskService.PermissionContext(other, null, null, true);
        Task otherTask = TaskService.createTask(otherContext, "solo", "private", Task.Importance.LOW, Task.Urgency.LOW);
        assertNotNull(otherTask);

        try {
            TagService.addTagToTask(
                new TaskService.PermissionContext(other, null, null, false),
                otherTask.getId(),
                tag.getId());
        } catch (SecurityException expected) {
            return;
        }
        throw new AssertionError("跨团队使用团队标签应被拒绝");
    }
}
