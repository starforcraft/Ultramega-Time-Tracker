package com.ultramega.timetracker.utils;

import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class LocalDateTimeConverter extends Converter<LocalDateTime> {
    public LocalDateTime fromString(@NotNull String value) {
        long epochMilli = Long.parseLong(value);
        ZoneId zoneId = ZoneId.systemDefault();
        return Instant.ofEpochMilli(epochMilli)
                .atZone(zoneId)
                .toLocalDateTime();
    }

    public String toString(LocalDateTime value) {
        ZoneId zoneId = ZoneId.systemDefault();
        long toEpochMilli = value.atZone(zoneId)
                .toInstant()
                .toEpochMilli();
        return Long.toString(toEpochMilli);
    }
}
