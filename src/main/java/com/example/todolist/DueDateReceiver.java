package com.example.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;

public class DueDateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("taskId", -1);
        String taskTitle = intent.getStringExtra("taskTitle");
        if (taskId == -1 || taskTitle == null) {
            return; // No valid data
        }

        // Create notification channel (for Android 8+)
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    ReminderManager.CHANNEL_ID,
                    "Due Date Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for task due dates");
            nm.createNotificationChannel(channel);
        }

        // Build the due date notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)  // app icon
                .setContentTitle("Task Due")
                .setContentText("Task \"" + taskTitle + "\" is due.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // When notification is tapped, open the app (MainActivity)
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, taskId, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(contentIntent);

        // Show the notification
        if (nm != null) {
            nm.notify(taskId, builder.build());
        }
    }
}
