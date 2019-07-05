package uk.co.epixstudios.skylark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("app_preferences", 0);

        Intent intent = new Intent(this, HistoryActivity.class);

        // Show settings screen the first time the app loads and until device is registered to a server
        if (settings.getString("server", "").equals("")) {
            intent = new Intent(this, SettingsActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
