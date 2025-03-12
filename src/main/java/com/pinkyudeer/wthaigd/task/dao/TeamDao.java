package com.pinkyudeer.wthaigd.task.dao;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.Team;

public class TeamDao {

    public static Integer insert(Team team) {
        return SQLHelper.insert(team);
    }

    public static UpdateBuilder<Team> update() {
        return SQLHelper.update(Team.class);
    }

    public static Integer updateByIdByCompare(Team team, Team oldTeam) {
        return SQLHelper.updateByCompare(team, oldTeam)
            .byId()
            .execute();
    }

    public static DeleteBuilder<Team> delete() {
        return SQLHelper.delete(Team.class);
    }

    public static Integer deleteById(Team team) {
        return SQLHelper.deleteById(team)
            .execute();
    }

    public static SelectBuilder<Team> select() {
        return SQLHelper.select(Team.class);
    }

    public static List<Team> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(Team.class);
    }
}
