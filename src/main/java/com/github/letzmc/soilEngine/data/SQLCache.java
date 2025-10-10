package com.github.letzmc.soilEngine.data;

import java.util.*;
import java.util.function.Function;

/**
 * Cache cho một cột trong bảng SQL
 */
public class SQLCache {

    private String tableName;
    private String columnName;
    private String[] primaryKeyNames;
    private String deleteQuery;
    private String selectQuery;
    private String updateQuery;
    private Map<SQLCacheEntry, Object> cache = Collections.synchronizedMap(new HashMap<>());
    private Set<SQLCacheEntry> modified = Collections.synchronizedSet(new HashSet<>());
    private SQLHelper sql;

    protected SQLCache(SQLHelper sql, String tableName, String columnName, String... primaryKeyNames) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.primaryKeyNames = primaryKeyNames;
        deleteQuery = "DELETE FROM " + this.tableName + " WHERE " + repeat(primaryKeyNames, " = ?", " AND ");
        selectQuery = "SELECT " + columnName + " FROM " + this.tableName + " WHERE " + repeat(primaryKeyNames, " = ?", " AND ");
        updateQuery = "UPDATE " + this.tableName + " SET " + columnName + " = ? WHERE " + repeat(primaryKeyNames, " = ?", " AND ");
        this.sql = sql;
    }

    private String repeat(String[] values, String str, String delimeter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]).append(str);
            if (i != values.length - 1) {
                builder.append(delimeter);
            }
        }
        return builder.toString();
    }

    /**
     * Lấy tên bảng
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Lấy tên cột
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Lấy tên các primary key
     */
    public String[] getPrimaryKeyNames() {
        return primaryKeyNames;
    }

    protected boolean keyNamesMatch(String[] matches) {
        for (String match : matches) {
            if (match.equals(columnName)) {
                return true;
            }
        }
        for (String key : primaryKeyNames) {
            for (String match : matches) {
                if (key.equals(match)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkKeys(Object... primaryKeys) {
        if (primaryKeys.length != primaryKeyNames.length) {
            throw new IllegalArgumentException("Expected " + primaryKeyNames.length + " primary keys, got " + primaryKeys.length);
        }
    }

    /**
     * Xóa dòng khỏi bảng và cache
     */
    public synchronized void delete(Object... primaryKeys) {
        remove(primaryKeys);
        sql.execute(deleteQuery, primaryKeys);
    }

    /**
     * Xóa giá trị khỏi cache (không ảnh hưởng đến bảng)
     */
    public synchronized void remove(Object... primaryKeys) {
        checkKeys(primaryKeys);
        SQLCacheEntry entry = new SQLCacheEntry(primaryKeys);
        modified.remove(entry);
        cache.remove(entry);
    }

    /**
     * Cập nhật giá trị trong cache
     */
    public synchronized void update(Object value, Object... primaryKeys) {
        checkKeys(primaryKeys);
        SQLCacheEntry entry = new SQLCacheEntry(primaryKeys);
        if (!cache.containsKey(entry)) {
            return;
        }
        cache.remove(entry);
        modified.add(entry);
        cache.put(entry, value);
    }

    /**
     * Lấy giá trị từ cache hoặc query nếu chưa có
     */
    public <T> T select(Object... primaryKeys) {
        return (T) select(o -> sql.querySingleResult(selectQuery, primaryKeys), primaryKeys);
    }

    /**
     * Lấy giá trị String từ cache
     */
    public String selectString(Object... primaryKeys) {
        return (String) select(o -> sql.querySingleResultString(selectQuery, primaryKeys), primaryKeys);
    }

    /**
     * Lấy giá trị Long từ cache
     */
    public Long selectLong(Object... primaryKeys) {
        return (Long) select(o -> sql.querySingleResultLong(selectQuery, primaryKeys), primaryKeys);
    }

    /**
     * Kiểm tra giá trị đã được cache chưa
     */
    public boolean isCached(Object... primaryKeys) {
        return cache.containsKey(new SQLCacheEntry(primaryKeys));
    }

    private synchronized Object select(Function<Object[], ?> supplier, Object... primaryKeys) {
        checkKeys(primaryKeys);
        SQLCacheEntry entry = new SQLCacheEntry(primaryKeys);
        Object value;
        if (!cache.containsKey(entry)) {
            value = supplier.apply(primaryKeys);
            cache.put(entry, value);
        } else {
            value = cache.get(entry);
        }
        return value;
    }

    /**
     * Xóa toàn bộ cache (mất tất cả thay đổi chưa flush)
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Lưu tất cả thay đổi trong cache vào database
     */
    public synchronized void flush() {
        modified.forEach(s -> {
            Object val = cache.get(s);
            Object[] objs = new Object[s.getParams().length + 1];
            objs[0] = val;
            for (int i = 0; i < s.getParams().length; i++) {
                objs[i + 1] = s.getParams()[i];
            }
            sql.execute(updateQuery, objs);
        });
        modified.clear();
    }

    /**
     * Lưu một giá trị cụ thể từ cache
     */
    public synchronized void flush(Object... primaryKeys) {
        SQLCacheEntry entry = new SQLCacheEntry(primaryKeys);
        Object val = cache.get(entry);
        if (val == null) {
            return;
        }
        Object[] objs = new Object[entry.getParams().length + 1];
        objs[0] = val;
        for (int i = 0; i < entry.getParams().length; i++) {
            objs[i + 1] = entry.getParams()[i];
        }
        sql.execute(updateQuery, objs);
        modified.remove(entry);
    }

}