package com.ddkolesnik.avitotraiding.repository;

import com.ddkolesnik.avitotraiding.utils.City;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexandr Stegnin
 */

public interface Verifiable {

    /**
     * Проверить город по шаблону
     *
     * @param address адрес
     * @param city город
     * @return результат проверки
     */
    default boolean checkCity(String address, City city) {
        String cityName = city.getName().toLowerCase();
        String template = "(%s)";
        String cityPattern = String.format(template, cityName);
        Pattern pattern = Pattern.compile(cityPattern);
        Matcher matcher = pattern.matcher(address.toLowerCase());
        return matcher.find();
    }

}
