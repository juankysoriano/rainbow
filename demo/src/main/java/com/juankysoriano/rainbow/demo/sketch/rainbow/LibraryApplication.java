package com.juankysoriano.rainbow.demo.sketch.rainbow;

import android.content.Context;

public abstract class LibraryApplication {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        LibraryApplication.context = context;
    }
}
