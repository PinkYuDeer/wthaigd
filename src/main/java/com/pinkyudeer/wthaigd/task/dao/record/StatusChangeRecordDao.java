package com.pinkyudeer.wthaigd.task.dao.record;

import java.sql.SQLException;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;
import com.pinkyudeer.wthaigd.task.entity.record.StatusChangeRecord;

public class StatusChangeRecordDao {

    public static Integer insert(StatusChangeRecord record) {
        return SQLHelper.insert(record);
    }

    public static UpdateBuilder<StatusChangeRecord> update() {
        return SQLHelper.update(StatusChangeRecord.class);
    }

    public static Integer updateByIdByCompare(StatusChangeRecord record, StatusChangeRecord oldRecord) {
        return SQLHelper.updateByCompare(record, oldRecord)
            .byId()
            .execute();
    }

    public static DeleteBuilder<StatusChangeRecord> delete() {
        return SQLHelper.delete(StatusChangeRecord.class);
    }

    public static Integer deleteById(StatusChangeRecord record) {
        return SQLHelper.deleteById(record)
            .execute();
    }

    public static SelectBuilder<StatusChangeRecord> select() {
        return SQLHelper.select(StatusChangeRecord.class);
    }

    public static List<StatusChangeRecord> selectAll() throws SQLException {
        return SQLHelper.selectAllFrom(StatusChangeRecord.class);
    }
}
