package uk.co.epixstudios.skylark;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(remoteMessage.getData());
    }

    private void sendNotification(Map<String, String> data) {
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(remotePicture)
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(myColor)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
