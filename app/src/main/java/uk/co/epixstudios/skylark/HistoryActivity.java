package uk.co.epixstudios.skylark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class HistoryActivity extends AppCompatActivity {
    static TableLayout tableLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeResources(
            R.color.colorAccent,
            R.color.colorPrimary);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNotifications();
            }
        });

        mSwipeRefreshLayout.setRefreshing(true);
        fetchNotifications();
    }

    private void onRefreshComplete() {
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            },
        1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                fetchNotifications();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void fetchNotifications() {
        SharedPreferences settings = getSharedPreferences("app_preferences", 0);
        String server = settings.getString("server", "http://127.0.0.1:8000/");
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
                    onRefreshComplete();
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
