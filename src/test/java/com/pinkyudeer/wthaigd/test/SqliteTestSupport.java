package com.pinkyudeer.wthaigd.test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.After;
import org.junit.Before;

import com.pinkyudeer.wthaigd.db.SQLiteManager;
import com.pinkyudeer.wthaigd.task.TaskSqlHelper;

public abstract class SqliteTestSupport {

    @Before
    public void openDatabase() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        setConnection(connection);
        SQLiteManager.isWorldLoaded = false;
        TaskSqlHelper.initTaskDataBase();
    }

    @After
    public void closeDatabase() {
        SQLiteManager.close();
    }

    private static void setConnection(Connection connection) throws Exception {
        Field field = SQLiteManager.class.getDeclaredField("inMemoryConnection");
        field.setAccessible(true);
        Connection oldConnection = (Connection) field.get(null);
        if (oldConnection != null && !oldConnection.isClosed()) oldConnection.close();
        field.set(null, connection);
    }
}
