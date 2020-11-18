package com.ddkolesnik.avitotraiding.utils;

/**
 * @author Alexandr Stegnin
 */

public class UrlUtils {

    private UrlUtils() {}

    //https://www.avito.ru/i57915260/rossiya/kommercheskaya_nedvizhimost/prodam-ASgBAgICAUSwCNJW?f=ASgBAgICAkSwCNJW8hKg2gE&q=%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%8C&sellerId=59cfc27e2f6f83525a502de38b0e74b2 - банк открытие
    //https://www.avito.ru/realty_rostelecom/rossiya/kommercheskaya_nedvizhimost/prodam-ASgBAgICAUSwCNJW?f=ASgBAgICAkSwCNJW8hKg2gE&q=%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%8C&s=104&sellerId=8016f339f4fdc113fe58a485abf87702 - ростелеком
    //https://www.avito.ru/sberbank_a/rossiya/kommercheskaya_nedvizhimost/prodam-ASgBAgICAUSwCNJW?f=ASgBAgICAkSwCNJW8hKg2gE&q=%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%8C&s=104&sellerId=93c406fa3fdb2fb05c00b8c37233cad7
    /**
     * Получить ссылку для определённого города
     *
     * @param city название города
     * @param company компания (Сбер, Открытие, Ростелеком)
     * @return ссылка на страницу объявлений
     */
    public static String getUrl(Company company, City city) {
        return "https://www.avito.ru/"
                .concat(company.getSystemName())
                .concat("/rossiya/kommercheskaya_nedvizhimost/")
                .concat("prodam-ASgBAgICAUSwCNJW?f=ASgBAgICAkSwCNJW8hKg2gE")
                .concat("&q=").concat(city.getName()).concat("&s=104")
                .concat("&sellerId=").concat(company.getSellerId());
    }

}
