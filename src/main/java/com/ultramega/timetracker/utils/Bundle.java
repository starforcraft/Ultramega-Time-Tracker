package com.ultramega.timetracker.utils;

import java.util.ResourceBundle;

public final class Bundle {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages.bundle");

    public static String message(String key) {
        return BUNDLE.getString(key);
    }
}
