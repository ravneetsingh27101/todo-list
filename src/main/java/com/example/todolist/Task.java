package com.example.todolist;

public class Task {
    private int id;
    private String title;
    private long dueDate; // milliseconds since epoch
    private boolean done;

    public Task(int id, String title, long dueDate, boolean done) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.done = done;
    }

    public Task(String title, long dueDate) {
        // Constructor for new tasks (id will be assigned by the database)
        this(0, title, dueDate, false);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public long getDueDate() { return dueDate; }
    public boolean isDone() { return done; }

    public void setTitle(String title) { this.title = title; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public void setDone(boolean done) { this.done = done; }
}
