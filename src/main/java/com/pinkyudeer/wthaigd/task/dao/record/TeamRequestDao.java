package com.pinkyudeer.wthaigd.task.dao.record;

import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.db.SQLHelper;
import com.pinkyudeer.wthaigd.db.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.db.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.db.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.record.TeamRequest;

public class TeamRequestDao {

    public static Integer insert(TeamRequest request) {
        return SQLHelper.insert(request);
    }

    public static UpdateBuilder<TeamRequest> update() {
        return SQLHelper.update(TeamRequest.class);
    }

    public static Integer updateByIdByCompare(TeamRequest request, TeamRequest oldRequest) {
        return SQLHelper.updateByCompare(request, oldRequest)
            .byId()
            .execute();
    }

    public static DeleteBuilder<TeamRequest> delete() {
        return SQLHelper.delete(TeamRequest.class);
    }

    public static SelectBuilder<TeamRequest> select() {
        return SQLHelper.select(TeamRequest.class);
    }

    public static TeamRequest selectById(UUID id) {
        return SQLHelper.selectByPremiereKey(TeamRequest.class, id);
    }

    public static List<TeamRequest> selectAll() {
        return SQLHelper.selectAllFrom(TeamRequest.class);
    }
}
