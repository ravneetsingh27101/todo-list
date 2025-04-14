package com.example.todolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderManager {
    public static final String CHANNEL_ID = "TASK_DUE_CHANNEL";

    // Schedule a notification alarm for the given task (at dueTime)
    public static void scheduleReminder(Context context, int taskId, String taskTitle, long dueTime) {
        if (dueTime <= System.currentTimeMillis()) return;  // don't schedule if time has passed

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DueDateReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("taskTitle", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+: use setExactAndAllowWhileIdle for exact timing even in Doze
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // API 19-22: use setExact
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, dueTime, pendingIntent);
            } else {
                // API <19: set() is inexact but it's the best we have
                alarmManager.set(AlarmManager.RTC_WAKEUP, dueTime, pendingIntent);
            }
        }
    }

    // Cancel a previously scheduled reminder for a given task
    public static void cancelReminder(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DueDateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
