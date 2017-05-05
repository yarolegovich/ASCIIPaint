package com.mrdeveloper.asciipaint;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
    }
}
