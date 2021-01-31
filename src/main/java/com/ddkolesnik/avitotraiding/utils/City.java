package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public enum City {

    TYUMEN("Тюмень", "%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%8C",
            "(тюменская\\s+обл)", "Тюменская"),
    EKB("Екатеринбург", "%D0%95%D0%BA%D0%B0%D1%82%D0%B5%D1%80%D0%B8%D0%BD%D0%B1%D1%83%D1%80%D0%B3",
            "(свердловская\\s+обл)", "Свердловская");

    private final String name;

    private final String urlEncoded;

    private final String pattern;

    private final String region;

    City(String name, String urlEncoded, String pattern, String region) {
        this.name = name;
        this.urlEncoded = urlEncoded;
        this.pattern = pattern;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public String getUrlEncoded() {
        return urlEncoded;
    }

    public String getPattern() {
        return pattern;
    }

    public String getRegion() {
        return region;
    }
}
