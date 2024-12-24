package com.ultramega.timetracker;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.ultramega.timetracker.utils.LocalDateTimeConverter;
import org.jetbrains.annotations.ApiStatus;

import java.time.LocalDateTime;
import java.util.HashMap;

@ApiStatus.Internal
public final class TimeTrackerData {
    public long idleTime;
    public long totalRunTime;
    public long totalDebugTime;

    public long todayTime;
    @OptionTag(converter = LocalDateTimeConverter.class)
    public LocalDateTime todayDateTime;
    public HashMap<String, HashMap<String, Long>> classTimeData = new HashMap<>();
    public boolean showTodayTimeOption = true;
}