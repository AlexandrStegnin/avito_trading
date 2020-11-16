package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public enum City {

    TYUMEN("Тюмень"),
    EKB("Екатеринбург");

    private final String name;

    City(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
