package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public enum Company {

    SBER("Сбербанк", "sberbank_a", "93c406fa3fdb2fb05c00b8c37233cad7"),
    OPENING("Открытие", "i57915260", "59cfc27e2f6f83525a502de38b0e74b2"),
    RT("Ростелеком", "realty_rostelecom", "8016f339f4fdc113fe58a485abf87702");

    private final String title;

    private final String systemName;

    private final String sellerId;

    Company(String title, String systemName, String sellerId) {
        this.title = title;
        this.systemName = systemName;
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getSellerId() {
        return sellerId;
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
