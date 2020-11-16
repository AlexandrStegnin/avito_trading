package com.ddkolesnik.avitotraiding.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import java.math.BigDecimal;

/**
 * @author Alexandr Stegnin
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TradingEntity extends AbstractEntity {

    /**
     * Лот
     */
    @Column(name = "lot")
    private String lot;

    /**
     * Описание
     */
    @Column(name = "description")
    private String description;

    /**
     * Адрес
     */
    @Column(name = "address")
    private String address;

    /**
     * Номер торгов
     */
    @Column(name = "trading_number")
    private String tradingNumber;

    /**
     * Идентификатор лота в ЕФРСБ
     */
    @Column(name = "efrsb_id")
    private String efrsbId;

    /**
     * Шаг аукциона
     */
    @Column(name = "auction_step")
    private String auctionStep;

    /**
     * Сумма задатка
     */
    @Column(name = "deposit_amount")
    private String depositAmount;

    /**
     * Время проведения торгов
     */
    @Column(name = "trading_time")
    private String tradingTime;

    /**
     * Период приёма заявок
     */
    @Column(name = "accept_requests_date")
    private String acceptRequestsDate;

    /**
     * Ссылка на лот
     */
    @Column(name = "url")
    private String url;

    /**
     * Стоимость
     */
    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "seller")
    private String seller;

}
