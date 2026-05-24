package com.technogenis.cafeteriacashier;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class CafeteriaApp extends Application {

    private static boolean persistenceEnabled = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!persistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            persistenceEnabled = true;
        }
    }
}
