package com.example.todolist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), TaskAdapter.OnTaskClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var taskList = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use androidx.appcompat.widget.Toolbar from the support library.
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize the database and load the tasks.
        dbHelper = DatabaseHelper(this)
        taskList = ArrayList(dbHelper.getAllTasks())

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList, dbHelper, this)
        recyclerView.adapter = taskAdapter

        // Setup FloatingActionButton click listener for adding new tasks.
        val fab = findViewById<FloatingActionButton>(R.id.fabAdd)
        fab.setOnClickListener {
            // Open AddEditTaskActivity to add a new task.
            val intent = Intent(this, AddEditTaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the task list after returning from the add/edit activity.
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        taskAdapter.notifyDataSetChanged()
    }

    override fun onTaskClick(task: Task) {
        // Open AddEditTaskActivity in edit mode (pass the task ID)
        val intent = Intent(this, AddEditTaskActivity::class.java)
        intent.putExtra("taskId", task.id)
        startActivity(intent)
    }

    override fun onTaskLongClick(task: Task) {
        // Show a confirmation dialog to delete the task.
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.delete_task))
        builder.setMessage(getString(R.string.delete_task_message))
        builder.setPositiveButton(getString(R.string.delete)) { _, _ ->
            // Cancel the reminder before deletion.
            ReminderManager.cancelReminder(this, task.id)
            dbHelper.deleteTask(task.id)
            val index = taskList.indexOf(task)
            if (index != -1) {
                taskList.removeAt(index)
                taskAdapter.notifyItemRemoved(index)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }
}
