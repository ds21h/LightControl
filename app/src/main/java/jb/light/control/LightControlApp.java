package jb.light.control;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LightControlApp extends Application {
    private static LightControlApp mLightControlApp;
    ExecutorService xExecutor;

    static LightControlApp getInstance(){
        return mLightControlApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLightControlApp = this;
        xExecutor = Executors.newCachedThreadPool();
    }
}
