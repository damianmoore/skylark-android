package uk.co.epixstudios.skylark;

import android.app.Application;
import android.content.Context;


public class AppActivity extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        AppActivity.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return AppActivity.context;
    }
}
