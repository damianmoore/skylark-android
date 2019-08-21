package uk.co.epixstudios.skylark;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Random;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String GROUP_KEY_DEFAULT = "uk.epixstudios.skylark.DEFAULT_GROUP";
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived");
        sendNotification(remoteMessage.getData());
    }

    private void sendNotification(Map<String, String> data) {
        Log.i(TAG, "sendNotification");
        Log.i(TAG, "id: " + data.get("id"));
        Log.i(TAG, "title: " + data.get("title"));
        Log.i(TAG, "body: " + data.get("body"));
        Log.i(TAG, "icon: " + data.get("icon"));

        Intent intent = new Intent(this, NotificationDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("NOTIFICATION_ID", data.get("id"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap remotePicture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        try {
            remotePicture = BitmapFactory.decodeStream((InputStream) new URL(data.get("icon")).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int myColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        try {
            myColor = Color.parseColor(data.get("color"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "high_importance";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "High Importance Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Notifications from Skylark that the sender has marked as having a high level of importance");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(myColor);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(AppActivity.getAppContext(), NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_notification)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setContentIntent(pendingIntent)
                .setLargeIcon(remotePicture)
                .setSound(defaultSoundUri)
                .setColor(myColor);

        notificationManager.notify(/*notification id*/new Random().nextInt(), notificationBuilder.build());
        Log.i(TAG, "notified");
    }

    public void onNewToken(String token) {
        Log.d(TAG, "New token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
        Log.i(TAG, "Send registration to server");
    }
}
