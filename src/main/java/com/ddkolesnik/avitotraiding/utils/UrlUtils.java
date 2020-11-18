package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public class UrlUtils {

    private UrlUtils() {}

    //https://www.avito.ru/sberbank_a/rossiya/kommercheskaya_nedvizhimost?q=Тюмень&sellerId=93c406fa3fdb2fb05c00b8c37233cad7
    //https://www.avito.ru/sberbank_a/rossiya?sellerId=93c406fa3fdb2fb05c00b8c37233cad7&page_from=from_shops_list - сбербанк
    //https://www.avito.ru/i57915260/rossiya?page_from=from_shops_list&q=%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%8C&sellerId=59cfc27e2f6f83525a502de38b0e74b2 - банк открытие
    //https://www.avito.ru/realty_rostelecom/rossiya?sellerId=8016f339f4fdc113fe58a485abf87702&page_from=from_shops_list - ростелеком
    /**
     * Получить ссылку для определённого города
     *
     * @param city название города
     * @param company компания (Сбер, Открытие, Ростелеком)
     * @return ссылка на страницу объявлений
     */
    public static String getUrl(String company, String city) {
        Company com = Company.fromSystemName(company);
        String mainPart = "https://www.avito.ru/";
        String withCompany = mainPart.concat(company);
        String middlePart = withCompany.concat("/rossiya/kommercheskaya_nedvizhimost?q=").concat(city).concat("&s=104");
        String result = middlePart.concat("&sellerId=").concat(com.getSellerId());
        return result;
    }

}
