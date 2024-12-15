package com.ultramega.timetracker.display;

public enum SelectableCharts {
    PIE_CHART("pie_chart"),
    TABLE("table");

    private final String name;

    SelectableCharts(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
