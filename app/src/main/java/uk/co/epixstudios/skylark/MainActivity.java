package uk.co.epixstudios.skylark;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                Snackbar.make(view, "Token: " + refreshedToken, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final SharedPreferences settings = getPreferences(0);
        boolean silent = settings.getBoolean("silentMode", false);


        final TextView deviceNameInput = (TextView) findViewById(R.id.input_device_name);
        deviceNameInput.setText(settings.getString("deviceName", Build.MANUFACTURER + " " + Build.MODEL));

        final TextView serverInput = (TextView) findViewById(R.id.input_server);
        serverInput.setText(settings.getString("server", "http://127.0.0.1:8000"));

        final TextView idInput = (TextView) findViewById(R.id.text_id);
        idInput.setText(settings.getString("id", ""));


        Button registerButton = (Button) findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            String url = serverInput.getText() + "/api/register/";

            Map<String, String> jsonParams = new HashMap<String, String>();
            jsonParams.put("name", deviceNameInput.getText().toString());

            if (settings.contains("id")) {
                jsonParams.put("id", settings.getString("id", ""));
            }

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if (!refreshedToken.isEmpty()) {
                jsonParams.put("firebase_token", refreshedToken);
            }

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
