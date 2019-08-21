package uk.co.epixstudios.skylark;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    String GROUP_KEY_DEFAULT = "uk.epixstudios.skylark.DEFAULT_GROUP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "SettingsActivity onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//            String refreshedToken = FirebaseInstanceId.getInstance().getInstanceId().toString();
//            Snackbar.make(view, "Token: " + refreshedToken, Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();

            FirebaseApp.initializeApp(AppActivity.getAppContext());

            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Log and toast
                    String msg = token;
                    Log.d(TAG, msg);
//                    Snackbar.make(view, "Token: " + token, Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    Toast.makeText(AppActivity.getAppContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        final SharedPreferences settings = getSharedPreferences("app_preferences", 0);
        boolean silent = settings.getBoolean("silentMode", false);


        final TextView deviceNameInput = (TextView) findViewById(R.id.input_device_name);
        deviceNameInput.setText(settings.getString("deviceName", Build.MANUFACTURER + " " + Build.MODEL));

        final TextView serverInput = (TextView) findViewById(R.id.input_server);
        String url = settings.getString("server", "http://127.0.0.1:8000/");
        if (!url.endsWith("/")) {
            url += '/';
        }
        serverInput.setText(url);

        final TextView idInput = (TextView) findViewById(R.id.text_id);
        idInput.setText(settings.getString("id", ""));


        Button registerButton = (Button) findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            String url = serverInput.getText() + "api/register/";

            Map<String, String> jsonParams = new HashMap<String, String>();
            jsonParams.put("name", deviceNameInput.getText().toString());

            if (settings.contains("id")) {
                jsonParams.put("id", settings.getString("id", ""));
            }

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if (!refreshedToken.isEmpty()) {
                jsonParams.put("firebase_token", refreshedToken);
            }
            Snackbar.make(view, refreshedToken, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(jsonParams),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences settings = getSharedPreferences("app_preferences", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        try {
                            editor.putString("deviceId", response.get("id").toString());
                        }
                        catch (JSONException e) {}

                        final TextView deviceNameInput = (TextView) findViewById(R.id.input_device_name);
                        editor.putString("deviceName", deviceNameInput.getText().toString());

                        final TextView serverInput = (TextView) findViewById(R.id.input_server);
                        editor.putString("server", serverInput.getText().toString());

                        editor.commit();

                        Snackbar.make(view, "Registered", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(view, "That didn't work!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            );

            queue.add(jsonObjectRequest);
            }
        });


        Button testNotificationButton = (Button) findViewById(R.id.button_test_notification);
        testNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.i(TAG, "onClick");

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String NOTIFICATION_CHANNEL_ID = "high_importance";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "High Importance Notifications", NotificationManager.IMPORTANCE_HIGH);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Notifications from Skylark that the sender has marked as having a low level of importance");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.CYAN);
                    notificationChannel.setVibrationPattern(new long[]{0, 250, 250, 250, 250, 250});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }


                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(AppActivity.getAppContext(), NOTIFICATION_CHANNEL_ID);

                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setContentTitle("Default notification")
                        .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                        .setContentInfo("Info")
                        .setVibrate(new long[]{0, 250, 250, 250, 250, 250});

                notificationManager.notify(/*notification id*/1, notificationBuilder.build());
                Log.i(TAG, "notified");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
