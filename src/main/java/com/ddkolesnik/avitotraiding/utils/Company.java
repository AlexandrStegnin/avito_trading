package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public enum Company {

    SBER("Сбербанк", "sberbank_a"),
    OPENING("Открытие", "i57915260"),
    RT("Ростелеком", "realty_rostelecom");

    private final String title;

    private final String systemName;

    Company(String title, String systemName) {
        this.title = title;
        this.systemName = systemName;
    }

    public String getTitle() {
        return title;
    }

    public String getSystemName() {
        return systemName;
    }

    public static Company fromSystemName(String systemName) {
        for (Company company : values()) {
            if (company.getSystemName().equalsIgnoreCase(systemName)) {
                return company;
            }
        }
        return SBER;
    }

}
