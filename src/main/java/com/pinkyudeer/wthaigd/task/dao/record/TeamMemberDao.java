package com.pinkyudeer.wthaigd.task.dao.record;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.record.TeamMember;

public class TeamMemberDao {

    public static Integer insert(TeamMember teamMember) {
        return SQLHelper.insert(teamMember);
    }

    public static UpdateBuilder<TeamMember> update() {
        return SQLHelper.update(TeamMember.class);
    }

    public static Integer updateByIdByCompare(TeamMember teamMember, TeamMember oldTeamMember) {
        return SQLHelper.updateByCompare(teamMember, oldTeamMember)
            .byId()
            .execute();
    }

    public static DeleteBuilder<TeamMember> delete() {
        return SQLHelper.delete(TeamMember.class);
    }

    public static Integer deleteById(TeamMember teamMember) {
        return SQLHelper.deleteById(teamMember)
            .execute();
    }

    public static SelectBuilder<TeamMember> select() {
        return SQLHelper.select(TeamMember.class);
    }

    public static List<TeamMember> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(TeamMember.class);
    }
}
