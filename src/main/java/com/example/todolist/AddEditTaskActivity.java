package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.DatePickerDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import java.text.DateFormat;
import java.util.Calendar;
import android.util.Log;

public class AddEditTaskActivity extends AppCompatActivity {

    private EditText editTitle;
    private Button btnDueDate;
    private Button btnSave;
    private Button btnCancel;
    private DatabaseHelper dbHelper;
    private Calendar dueDateCalendar;
    private int taskId = -1;
    private boolean isEditing = false;
    private boolean initialDone = false;

    private static final String TAG = "AddEditTaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // Setup the top App Bar (Toolbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTitle = findViewById(R.id.editTitle);
        btnDueDate = findViewById(R.id.btnDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        dbHelper = new DatabaseHelper(this);
        dueDateCalendar = Calendar.getInstance();

        // Check if an existing task is being edited
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("taskId")) {
            taskId = intent.getIntExtra("taskId", -1);
            if (taskId != -1) {
                isEditing = true;
                Task task = dbHelper.getTask(taskId);
                if (task != null) {
                    editTitle.setText(task.getTitle());
                    initialDone = task.isDone();
                    dueDateCalendar.setTimeInMillis(task.getDueDate());
                    updateDueDateButtonText();
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(R.string.edit_task);
                } else {
                    Log.e(TAG, "Task with id " + taskId + " not found.");
                    Toast.makeText(this, "Task not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        } else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.add_task);
        }

        // Due Date button opens a DatePicker
        btnDueDate.setOnClickListener(v -> {
            int year = dueDateCalendar.get(Calendar.YEAR);
            int month = dueDateCalendar.get(Calendar.MONTH);
            int day = dueDateCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog picker = new DatePickerDialog(
                    AddEditTaskActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        // Set the chosen date in our Calendar
                        dueDateCalendar.set(Calendar.YEAR, year1);
                        dueDateCalendar.set(Calendar.MONTH, month1);
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        // Set a fixed reminder time at 9:00 AM on that date
                        dueDateCalendar.set(Calendar.HOUR_OF_DAY, 9);
                        dueDateCalendar.set(Calendar.MINUTE, 0);
                        dueDateCalendar.set(Calendar.SECOND, 0);
                        dueDateCalendar.set(Calendar.MILLISECOND, 0);
                        updateDueDateButtonText();
                    },
                    year, month, day);
            picker.show();
        });

        // Save button logic
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(AddEditTaskActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }
            long dueTime = dueDateCalendar.getTimeInMillis();

            if (!isEditing) {
                // New task: ensure a due date has been selected
                if (btnDueDate.getText().toString().equals(getString(R.string.select_due_date))) {
                    Toast.makeText(AddEditTaskActivity.this, "Please select a due date", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Insert the new task into the database
                int newId = dbHelper.addTask(title, dueTime, false);
                // Schedule reminder if due date is in the future
                if (dueTime > System.currentTimeMillis()) {
                    ReminderManager.scheduleReminder(AddEditTaskActivity.this, newId, title, dueTime);
                }
            } else {
                // Existing task: update the record
                dbHelper.updateTask(taskId, title, dueTime, initialDone);
                // Reset any previous alarm and schedule a new one if needed
                ReminderManager.cancelReminder(AddEditTaskActivity.this, taskId);
                if (!initialDone && dueTime > System.currentTimeMillis()) {
                    ReminderManager.scheduleReminder(AddEditTaskActivity.this, taskId, title, dueTime);
                }
            }
            finish();
        });

        // Cancel button discards changes and closes
        btnCancel.setOnClickListener(v -> finish());
    }

    // Update the due date button text to reflect the selected date
    private void updateDueDateButtonText() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        btnDueDate.setText("Due: " + dateFormat.format(dueDateCalendar.getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
