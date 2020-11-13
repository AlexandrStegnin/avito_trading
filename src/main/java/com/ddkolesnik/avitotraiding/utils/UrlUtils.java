package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public class UrlUtils {

    private UrlUtils() {}

    //https://www.avito.ru/sberbank_a/rossiya/kommercheskaya_nedvizhimost?q=Тюмень

    /**
     * Получить ссылку для определённого города
     *
     * @param city название города
     * @return ссылка на страницу объявлений
     */
    public static String getUrl(String city) {
        return "https://www.avito.ru/sberbank_a/rossiya/kommercheskaya_nedvizhimost?q=" + city;
    }

}
