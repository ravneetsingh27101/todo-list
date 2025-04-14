package com.example.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_TASKS = "tasks";
    // Column names
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DUE_DATE = "due_date";
    public static final String COL_DONE = "done";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the tasks table
        String sql = "CREATE TABLE " + TABLE_TASKS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_DUE_DATE + " INTEGER, " +
                COL_DONE + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop and recreate table on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // Insert a new task and return its new ID
    public int addTask(String title, long dueDate, boolean done) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_DONE, done ? 1 : 0);
        long insertId = db.insert(TABLE_TASKS, null, values);
        return (int) insertId;
    }

    // Retrieve all tasks in the database
    public List<Task> getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COL_ID, COL_TITLE, COL_DUE_DATE, COL_DONE},
                null, null, null, null,
                COL_DUE_DATE + " ASC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                long due = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE));
                boolean done = (cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONE)) == 1);
                tasks.add(new Task(id, title, due, done));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tasks;
    }

    // Retrieve a single task by its ID
    public Task getTask(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Task task = null;
        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COL_ID, COL_TITLE, COL_DUE_DATE, COL_DONE},
                COL_ID + "=?",
                new String[]{String.valueOf(taskId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
            long due = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE));
            boolean done = (cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONE)) == 1);
            task = new Task(taskId, title, due, done);
        }
        cursor.close();
        return task;
    }

    // Update an existing task's details
    public void updateTask(int id, String title, long dueDate, boolean done) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_DONE, done ? 1 : 0);
        db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Update only the 'done' status of a task
    public void updateTaskDone(int id, boolean done) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DONE, done ? 1 : 0);
        db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete a task by ID
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}
