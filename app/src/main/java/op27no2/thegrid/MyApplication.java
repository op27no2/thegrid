package op27no2.thegrid;

import android.content.Context;

public class MyApplication extends android.app.Application {
    private static Context context;

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        System.out.println("APPLICATION CALLED");

    }


    public static Context getAppContext() {
        return MyApplication.context;
    }



}
