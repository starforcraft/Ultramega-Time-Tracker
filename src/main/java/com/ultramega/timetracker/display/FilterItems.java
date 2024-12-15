package com.ultramega.timetracker.display;

public enum FilterItems {
    TWELVE_HOURS("12hours"),
    TWENTY_FOUR_HOURS("24hours"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year");
    //TOTAL("total");

    private final String name;

    FilterItems(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
