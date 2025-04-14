package com.example.todolist;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private DatabaseHelper dbHelper;
    private OnTaskClickListener listener;

    // Interface for click callbacks
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, DatabaseHelper dbHelper, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView titleView;
        TextView dueDateView;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            titleView = itemView.findViewById(R.id.textTitle);
            dueDateView = itemView.findViewById(R.id.textDueDate);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set task title and format due date.
        holder.titleView.setText(task.getTitle());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        holder.dueDateView.setText("Due: " + dateFormat.format(new Date(task.getDueDate())));

        // Remove any previous listener to avoid unwanted triggers.
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone());

        // Set new checkbox listener with defensive coding.
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                task.setDone(isChecked);
                dbHelper.updateTaskDone(task.getId(), isChecked);

                if (isChecked) {
                    // When marking as done, cancel any reminder.
                    ReminderManager.cancelReminder(holder.itemView.getContext(), task.getId());
                } else {
                    // When unchecking, if the task's due date is in the future,
                    // schedule a reminder.
                    long now = System.currentTimeMillis();
                    if (task.getDueDate() > now) {
                        String titleForReminder = (task.getTitle() != null) ? task.getTitle() : "";
                        ReminderManager.scheduleReminder(holder.itemView.getContext(), task.getId(), titleForReminder, task.getDueDate());
                    }
                }

                // Update visual feedback: add/remove strike-through.
                if (isChecked) {
                    holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            } catch (Exception e) {
                Log.e("TaskAdapter", "Error updating check state", e);
            }
        });

        // Set click listener for the whole item view.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTaskLongClick(task);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
