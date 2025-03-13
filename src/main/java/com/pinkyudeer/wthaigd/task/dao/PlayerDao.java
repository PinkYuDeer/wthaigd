package com.pinkyudeer.wthaigd.task.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.Player;

public class PlayerDao {

    public static Integer insert(Player player) {
        return SQLHelper.insert(player);
    }

    public static UpdateBuilder<Player> update() {
        return SQLHelper.update(Player.class);
    }

    public static Integer updateByIdByCompare(Player player, Player oldPlayer) {
        return SQLHelper.updateByCompare(player, oldPlayer)
            .byId()
            .execute();
    }

    public static DeleteBuilder<Player> delete() {
        return SQLHelper.delete(Player.class);
    }

    public static Integer deleteById(Player player) {
        return SQLHelper.deleteById(player)
            .execute();
    }

    public static SelectBuilder<Player> select() {
        return SQLHelper.select(Player.class);
    }

    public static List<Player> selectAll() {
        return SQLHelper.selectAllFrom(Player.class);
    }

    public static Player selectById(UUID uuid) {
        return SQLHelper.selectByPremiereKey(Player.class, uuid);
    }

    public static void updateOrInsert(EntityPlayer player) {
        Player entity = selectById(player.getUniqueID());
        if (entity == null) {
            entity = new Player(player);
            SQLHelper.insert(entity);
        } else {
            Player oldEntity = UtilHelper.deepClone(entity, Player.class);
            entity.setPlayer(player);
            entity.setLastLoginTime(LocalDateTime.now());
            SQLHelper.updateByCompare(entity, oldEntity)
                .byId()
                .execute();
        }
    }
}
