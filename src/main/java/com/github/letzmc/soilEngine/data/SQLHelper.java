package com.github.letzmc.soilEngine.data;

import com.github.letzmc.soilEngine.SoilEngine;
import com.github.letzmc.soilEngine.misc.Task;

import java.io.Closeable;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Lớp hỗ trợ SQL với các phương thức tiện ích
 */
public class SQLHelper implements Closeable {

    /**
     * Mở kết nối đến cơ sở dữ liệu SQLite
     */
    public static Connection openSQLite(Path file) {
        try {
            Class.forName("org.sqlite.JDBC");

            final Properties properties = new Properties();
            properties.setProperty("foreign_keys", "on");
            properties.setProperty("busy_timeout", "1000");

            return DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath(), properties);
        } catch (ClassNotFoundException | SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Mở kết nối đến cơ sở dữ liệu MySQL
     */
    public static Connection openMySQL(String ip, int port, String username, String password, String database) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/?user=" + username + "&password=" + password);
            connection.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + database + ";");
            connection.createStatement().execute("USE " + database + ";");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Mở kết nối đến MySQL tại localhost:3306
     */
    public static Connection openMySQL(String username, String password, String database) {
        return openMySQL("localhost", 3306, username, password, database);
    }

    private static <T extends Exception> void sneakyThrow(Exception e) throws T {
        throw (T) e;
    }

    private Connection connection;
    private List<SQLCache> caches = new ArrayList<>();
    private Task commitTask = null;

    /**
     * Tạo SQLHelper từ Connection
     */
    public SQLHelper(Connection connection) {
        this.connection = connection;
    }

    /**
     * Tạo cache cho một cột
     */
    public SQLCache createCache(String tableName, String columnName, String... primaryKeyNames) {
        SQLCache cache = new SQLCache(this, tableName, columnName, primaryKeyNames);
        caches.add(cache);
        return cache;
    }

    /**
     * Xả cache khớp với pattern
     */
    public void flushMatchingCaches(String pattern, Object... primaryKeys) {
        getMatchingCaches(pattern).forEach(c -> c.flush(primaryKeys));
    }

    /**
     * Xóa entry từ cache khớp với pattern
     */
    public void removeFromMatchingCaches(String pattern, Object... primaryKeys) {
        getMatchingCaches(pattern).forEach(c -> c.remove(primaryKeys));
    }

    /**
     * Xả và xóa entry từ cache khớp với pattern
     */
    public void flushAndRemoveFromMatchingCaches(String pattern, Object... primaryKeys) {
        List<SQLCache> caches = getMatchingCaches(pattern);
        caches.forEach(c -> c.flush(primaryKeys));
        caches.forEach(c -> c.remove(primaryKeys));
    }

    /**
     * Lấy cache khớp với pattern
     */
    public List<SQLCache> getMatchingCaches(String pattern) {
        List<SQLCache> list = new ArrayList<>();
        String[] split = pattern.split("\\.");
        if (split.length != 2) {
            throw new IllegalArgumentException("Pattern to match caches must match tableName.columnName (use * to match all of either)");
        }
        String[] tableName = split[0].split("\\|");
        String[] columnName = split[1].split("\\|");
        for (SQLCache cache : caches) {
            if (!(tableName[0].equals("*") || Arrays.stream(tableName).anyMatch(s -> s.equals(cache.getTableName())))) {
                continue;
            }
            if (!(columnName[0].equals("*") || cache.keyNamesMatch(columnName))) {
                continue;
            }
            list.add(cache);
        }
        return list;
    }

    /**
     * Lấy danh sách cache
     */
    public List<SQLCache> getCaches() {
        return caches;
    }

    /**
     * Xả tất cả cache
     */
    public void flushAllCaches() {
        caches.forEach(SQLCache::flush);
    }

    /**
     * Xóa tất cả cache
     */
    public void clearAllCaches() {
        caches.forEach(SQLCache::clear);
    }

    /**
     * Thực thi lệnh SQL
     */
    public void execute(String command, Object... fields) {
        try {
            PreparedStatement statement = prepareStatement(command, fields);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    /**
     * Thực thi lệnh SQL và trả về số dòng được cập nhật
     */
    public int executeUpdate(String command, Object... fields) {
        int updatedRows = 0;
        try {
            PreparedStatement statement = prepareStatement(command, fields);
            updatedRows = statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return updatedRows;
    }

    /**
     * Thực thi query và trả về kết quả đầu tiên
     */
    public <T> T querySingleResult(String query, Object... fields) {
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return null;
            }
            T obj = (T) results.getObject(1);
            results.close();
            statement.close();
            return obj;
        } catch (SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Thực thi query và trả về kết quả đầu tiên dạng String
     */
    public String querySingleResultString(String query, Object... fields) {
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return null;
            }
            String val = results.getString(1);
            results.close();
            statement.close();
            return val;
        } catch (SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Thực thi query và trả về kết quả đầu tiên dạng bytes
     */
    public byte[] querySingleResultBytes(String query, Object... fields) {
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return null;
            }
            byte[] val = results.getBytes(1);
            results.close();
            statement.close();
            return val;
        } catch (SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Thực thi query và trả về kết quả đầu tiên dạng Long
     */
    public Long querySingleResultLong(String query, Object... fields) {
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return null;
            }
            long val = results.getLong(1);
            results.close();
            statement.close();
            return val;
        } catch (SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Thực thi query và trả về danh sách kết quả
     */
    public <T> List<T> queryResultList(String query, Object... fields) {
        List<T> list = new ArrayList<>();
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                list.add((T) results.getObject(1));
            }
            results.close();
            statement.close();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return list;
    }

    /**
     * Thực thi query và trả về danh sách kết quả dạng String
     */
    public List<String> queryResultStringList(String query, Object... fields) {
        List<String> list = new ArrayList<>();
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                list.add(results.getString(1));
            }
            results.close();
            statement.close();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return list;
    }

    /**
     * Thực thi query và trả về Results
     */
    public Results queryResults(String query, Object... fields) {
        try {
            PreparedStatement statement = prepareStatement(query, fields);
            ResultSet results = statement.executeQuery();
            return new Results(results, statement);
        } catch (SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Lấy Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Thiết lập auto-commit
     */
    public void setAutoCommit(boolean autoCommit) {
        try {
            setCommitInterval(-1);
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            sneakyThrow(e);

        }
    }

    /**
     * Kiểm tra auto-commit
     */
    public boolean isAutoCommit() {
        try {
            return connection.getAutoCommit();
        } catch (SQLException e) {
            sneakyThrow(e);
            return false;
        }
    }

    /**
     * Thiết lập khoảng thời gian commit tự động
     */
    public void setCommitInterval(int ticks) {
        if (commitTask != null) {
            commitTask.cancel();
            commitTask = null;
        }
        if (ticks == -1) {
            return;
        }
        setAutoCommit(false);
        commitTask = Task.syncRepeating(SoilEngine.getInstance(), this::commit, ticks, ticks);
    }

    /**
     * Xả cache và commit transaction
     */
    public void commit() {
        try {
            flushAllCaches();
            connection.commit();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    /**
     * Chuẩn bị PreparedStatement
     */
    public PreparedStatement prepareStatement(String query, Object... fields) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            int i = 1;
            for (Object object : fields) {
                statement.setObject(i, object);
                i++;
            }
            return statement;
        } catch (SQLException e) {
            sneakyThrow(e);
            return null;
        }
    }

    /**
     * Đóng kết nối
     */
    @Override
    public void close() {
        try {
            setCommitInterval(-1);
            connection.close();
            connection = null;
            System.gc();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    /**
     * Wrapper cho ResultSet dễ sử dụng hơn
     */
    public static class Results implements AutoCloseable {

        private ResultSet results;
        private boolean empty;
        private PreparedStatement statement;

        private Results(ResultSet results, PreparedStatement statement) {
            this.results = results;
            this.statement = statement;
            try {
                empty = !results.next();
            } catch (SQLException e) {
                sneakyThrow(e);
            }
        }

        /**
         * Kiểm tra kết quả có trống không
         */
        public boolean isEmpty() {
            return empty;
        }

        /**
         * Chuyển đến dòng tiếp theo
         */
        public boolean next() {
            try {
                return results.next();
            } catch (SQLException e) {
                sneakyThrow(e);
                return false;
            }
        }

        /**
         * Thực hiện thao tác trên mỗi dòng
         */
        public void forEach(Consumer<Results> lambda) {
            if (isEmpty()) {
                return;
            }
            lambda.accept(this);
            while (next()) {
                lambda.accept(this);
            }
            close();
        }

        /**
         * Lấy giá trị từ cột
         */
        public <T> T get(int column) {
            try {
                return (T) results.getObject(column);
            } catch (SQLException e) {
                sneakyThrow(e);
                return null;
            }
        }

        /**
         * Lấy bytes từ cột
         */
        public byte[] getBytes(int column) {
            try {
                return results.getBytes(column);
            } catch (SQLException e) {
                sneakyThrow(e);
                return null;
            }
        }

        /**
         * Lấy String từ cột
         */
        public String getString(int column) {
            try {
                return results.getString(column);
            } catch (SQLException e) {
                sneakyThrow(e);
                return null;
            }
        }

        /**
         * Lấy Long từ cột
         */
        public Long getLong(int column) {
            try {
                return results.getLong(column);
            } catch (SQLException e) {
                sneakyThrow(e);
                return null;
            }
        }

        /**
         * Lấy số lượng cột
         */
        public int getColumnCount() {
            try {
                return results.getMetaData().getColumnCount();
            } catch (SQLException e) {
                sneakyThrow(e);
                return 0;
            }
        }

        /**
         * Đóng ResultSet
         */
        @Override
        public void close() {
            try {
                results.close();
                statement.close();
            } catch (SQLException e) {
                sneakyThrow(e);
            }
        }

    }

}