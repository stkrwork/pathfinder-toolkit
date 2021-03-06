package com.lateensoft.pathfinder.toolkit.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lateensoft.pathfinder.toolkit.dao.DataAccessException;
import com.lateensoft.pathfinder.toolkit.dao.GenericDAO;
import com.lateensoft.pathfinder.toolkit.db.CursorUtil;
import com.lateensoft.pathfinder.toolkit.db.Database;
import org.jetbrains.annotations.Nullable;
import roboguice.RoboGuice;

import java.util.Hashtable;
import java.util.List;

public abstract class GenericTableDAO<RowId, Entity, RowData> implements GenericDAO<Entity> {
    private Database database;
    private Table table;

    public GenericTableDAO(Context context) {
        database = RoboGuice.getInjector(context).getInstance(Database.class);
        table = initTable();
    }

    protected abstract Table initTable();

    public Entity find(RowId id) {
        String selector = andSelectors(getIdSelector(id), getBaseSelector());
        String tables = getFromQueryClause();
        String[] columns = getColumnsForQuery();
        Cursor cursor = database.query(tables, columns, selector);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            Hashtable<String, Object> hashTable =  CursorUtil.getTableOfValues(cursor);
            return buildFromHashTable(hashTable);
        } else {
            return null;
        }
    }

    protected List<String> getTablesForQuery() {
        return Lists.newArrayList(getTable().getName());
    }

    protected String getFromQueryClause() {
        return Joiner.on(", ").join(getTablesForQuery());
    }

    protected String[] getColumnsForQuery() {
        return getTable().getColumnNames();
    }

    public boolean exists(RowId id) {
        Cursor cursor= getDatabase().rawQuery("select count(*) count from " + getTable().getName() + " where " +
                getIdSelector(id), null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count != 0;
    }

    @Override
    public List<Entity> findAll() {
        return findFiltered(getBaseSelector(), getDefaultOrderBy());
    }

    public List<Entity> findFiltered(String selector, String orderBy) {
        String table = getFromQueryClause();
        String[] columns = getColumnsForQuery();
        return findFiltered(table, columns, selector, orderBy);
    }

    public List<Entity> findFiltered(String tables, String[] columns, String selector, String orderBy) {
        Cursor cursor = database.query(true, tables, columns, selector,
                null, null, null, orderBy, null);

        List<Entity> entities = Lists.newArrayListWithCapacity(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Hashtable<String, Object> hashTable = CursorUtil.getTableOfValues(cursor);
            entities.add(buildFromHashTable(hashTable));
            cursor.moveToNext();
        }
        return entities;
    }

    public void remove(RowData rowData) throws DataAccessException {
        removeById(getIdFromRowData(rowData));
    }

    public void removeById(RowId id) throws DataAccessException {
        String selector = getIdSelector(id);
        String table = getTable().getName();
        int returnVal = database.delete(table, selector);
        if (returnVal <= 0) {
            throw new DataAccessException("Database.delete for " + id + " returned " + returnVal);
        }
    }

    public RowId add(RowData rowData) throws DataAccessException {
        try {
            beginTransaction();
            ContentValues values = getContentValues(rowData);
            String table = getTable().getName();
            long id = getDatabase().insert(table, values);
            if (id == -1) {
                throw new DataAccessException("Failed to insert " + rowData);
            }

            if (!isIdSet(rowData)) {
                setId(rowData, id);
            }
            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
        return getIdFromRowData(rowData);
    }

    protected void beginTransaction() {
        database.beginTransaction();
    }

    protected void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    protected void endTransaction() {
        database.endTransaction();
    }

    protected abstract boolean isIdSet(RowData entity);

    protected abstract void setId(RowData entity, long id);

    public void update(RowData rowData) throws DataAccessException {
        try {
            beginTransaction();
            String selector = getIdSelector(getIdFromRowData(rowData));
            ContentValues values = getContentValues(rowData);
            String table = getTable().getName();
            int returnVal = getDatabase().update(table, values, selector);
            if (returnVal <= 0) {
                throw new DataAccessException("Failed to update (code " + returnVal + ") " + rowData);
            }
            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    protected abstract RowId getIdFromRowData(RowData rowData);

    protected abstract ContentValues getContentValues(RowData rowData);

    protected abstract String getIdSelector(RowId id);

    protected abstract Entity buildFromHashTable(Hashtable<String, Object> hashTable);

    protected @Nullable String getBaseSelector() {
        return null;
    }

    protected String getDefaultOrderBy() {
        return null;
    }

    protected Database getDatabase() {
        return database;
    }

    public Table getTable() {
        return table;
    }

    public static String andSelectors(String... selectors) {
        String selector = Joiner.on(" AND ").skipNulls().join(selectors);
        if (!selector.isEmpty()) {
            return selector;
        } else {
            return null;
        }
    }
}
