package com.ddkolesnik.avitotraiding.model;

import lombok.Data;

/**
 * @author Alexandr Stegnin
 */

@Data
public class TradingEntity {

    /**
     * Лот
     */
    private String lot;

    /**
     * Описание
     */
    private String description;

    /**
     * Адрес
     */
    private String address;

    /**
     * Номер торгов
     */
    private String tradingNumber;

    /**
     * Идентификатор лота в ЕФРСБ
     */
    private String efrsbId;

    /**
     * Шаг аукциона
     */
    private String auctionStep;

    /**
     * Сумма задатка
     */
    private String depositAmount;

    /**
     * Время проведения торгов
     */
    private String tradingTime;

    /**
     * Период приёма заявок
     */
    private String acceptRequestsDate;

    /**
     * Ссылка на лот
     */
    private String lotUrl;

}
