package uk.co.epixstudios.skylark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class HistoryActivity extends AppCompatActivity {
    static TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        tableLayout = (TableLayout) findViewById(R.id.table_notifications);

        SharedPreferences settings = getSharedPreferences("app_preferences", 0);
        final String server = settings.getString("server", "http://127.0.0.1:8000/");
        this.fetchNotifications(server);
    }

    void addItem(Context context, String id, String type, String date, String content) {
        final View item = LayoutInflater.from(context).inflate(R.layout.item_notification, tableLayout, false);

        final TextView typeText = (TextView) item.findViewById(R.id.type);
        final TextView dateText = (TextView) item.findViewById(R.id.date);
        final TextView contentText = (TextView) item.findViewById(R.id.content);
        final String notificationId = id;

        typeText.setText(type);
        dateText.setText(date);
        contentText.setText(content);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, NotificationDetailActivity.class);
                intent.putExtra("NOTIFICATION_ID", notificationId);
                startActivity(intent);
            }
        });

        tableLayout.addView(item);
    }

    void fetchNotifications(String server) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = server + "api/notifications/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray results = null;
                        try {
                            results = response.getJSONArray("results");
                            for (int i = 0, size = results.length(); i < size; i++) {
                                JSONObject o = results.getJSONObject(i);
                                HistoryActivity.this.addItem(HistoryActivity.this, o.getString("id"), o.getString("webhook"), formatDate(o.getString("created")), o.getString("title"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}
        }
        );

        queue.add(jsonObjectRequest);
    }

    String formatDate(String dateStr) {
        Date now = Calendar.getInstance().getTime();
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat dateAndTime = new SimpleDateFormat("d MMM kk:mm");
        dateAndTime.setTimeZone(TimeZone.getDefault());
        DateFormat timeOnly = new SimpleDateFormat("kk:mm");
        dateAndTime.setTimeZone(TimeZone.getDefault());
        try {
            Date parsedDate = isoFormat.parse(dateStr);
            if (now.getTime() - parsedDate.getTime() < 24*60*60*1000) {
                dateStr = timeOnly.format(parsedDate);
            }
            else {
                dateStr = dateAndTime.format(parsedDate);
            }
        }
        catch (java.text.ParseException e) {
            dateStr = e.toString();
        }
        return dateStr;
    }
}
