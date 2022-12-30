package com.example.firstapplication.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.firstapplication.R;
import com.example.firstapplication.SeparateHistoryActivity;
import com.example.firstapplication.db.DatabaseHandler;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver{
    private static final String CHANNEL_ID = "qr-giuse-syncing";
    private DatabaseHandler databaseHandler = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        databaseHandler = new DatabaseHandler(context);
        if(databaseHandler.checkHaveNonSyncedAttendance())
            createNotification(context);
    }

    private void createNotification(Context context){
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
            if(channel == null){
                channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_notification_title),
                        NotificationManager.IMPORTANCE_HIGH);
                // config notification channel
                channel.setDescription("The channel is used for syncing attendance data");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 1000, 200, 340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }
        Intent notificationIntent = new Intent(context, SeparateHistoryActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity
                    (context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.giuse_church)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.giuse_church)).bigLargeIcon(null))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100, 1000, 200, 340})
                .setAutoCancel(false)
                .setTicker("Notification");
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(0, builder.build());
    }
}