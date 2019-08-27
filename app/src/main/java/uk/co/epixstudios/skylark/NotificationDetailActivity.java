package uk.co.epixstudios.skylark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class NotificationDetailActivity extends AppCompatActivity {

    static TableLayout tableVariables;
    private static Context mContext;
    private static final String TAG = "NotificationDetailActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tableVariables = (TableLayout) findViewById(R.id.table_variables);

        SharedPreferences settings = getSharedPreferences("app_preferences", 0);

        final String server = settings.getString("server", "http://127.0.0.1:8000");

        String notificationId = getIntent().getStringExtra("NOTIFICATION_ID");
        Log.i(TAG, "notificationId = " + notificationId);

        this.addTableRow(this,"notificationId", notificationId);

        this.fetchParameters(server, notificationId);

        mContext = this;
    }

    static void addTableRow(Context context, String key, String val) {
        final View item = LayoutInflater.from(context).inflate(R.layout.item_variable, tableVariables, false);

        final TextView keyText = (TextView) item.findViewById(R.id.key);
        final TextView valText = (TextView) item.findViewById(R.id.val);

        keyText.setText(key);
        valText.setText(val);

        if (val.startsWith("https://") || val.startsWith("http://")) {
            final String url = val;
            valText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intent intent = new Intent(mContext, WebviewActivity.class);
                intent.putExtra("URL", url);
                mContext.startActivity(intent);
                }
            });
        }
        tableVariables.addView(item);
    }

    void fetchParameters(String server, String notificationId) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = server + "api/notification/" + notificationId + "/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Iterator<?> keys = response.keys();
                    while(keys.hasNext()) {
                        String key = (String)keys.next();
                        if (!key.equals("id")) {
                            try {
                                NotificationDetailActivity.this.addTableRow(NotificationDetailActivity.this, key, response.get(key).toString());
                            } catch (JSONException e) {}
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            }
        );

        queue.add(jsonObjectRequest);
    }
}
