package com.ultramega.timetracker.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;

public final class Utils {
    @SuppressWarnings("UseJBColor")
    public static Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static String convertSecondsToTime(long totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long convertTimeToSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600L + minutes * 60L + seconds;
    }

    public static Comparator<String> compareTimes() {
        return (time1, time2) -> {
            long totalSeconds1 = Utils.convertTimeToSeconds(time1);
            long totalSeconds2 = Utils.convertTimeToSeconds(time2);
            return Long.compare(totalSeconds1, totalSeconds2);
        };
    }

    public static String getCurrentTimeAsString() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        return now.format(formatter);
    }

    public static Date convertStringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());

        return Date.from(zonedDateTime.toInstant());
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Nullable
    public static VirtualFile getOpenedFile(Project project) {
        if (project.isDisposed())
            return null;

        VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();

        if (selectedFiles.length == 0)
            return null;

        return selectedFiles[0];
    }
}
