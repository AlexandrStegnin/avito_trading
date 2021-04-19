package com.ddkolesnik.avitotraiding.service;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.avitotraiding.model.TradingEntity;
import com.ddkolesnik.avitotraiding.repository.Verifiable;
import com.ddkolesnik.avitotraiding.utils.City;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.*;

/**
 * Севрис для получения информации о торгах с площадки РТС
 *
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class RtsGrabberService implements Verifiable {

    private final TradingService tradingService;

    private final static String RTS_FILTERED_URL = "https://it2.rts-tender.ru/?priceFrom=1000000" +
            "&procedureTypeNames=%D0%9F%D1%80%D0%B8%D0%B2%D0%B0%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%20%D0%BC%D1%83%D0%BD%D0%B8%D1%86%D0%B8%D0%BF%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B0%D1%80%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%B0%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0,%20%D0%BE%D0%B1%D1%80%D0%B0%D1%89%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B2%20%D1%81%D0%BE%D0%B1%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D1%81%D1%82%D1%8C%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20(%D0%BF%D1%80%D0%BE%D0%B4%D0%B0%D0%B6%D0%B0)%20%D0%BD%D0%B5%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%20%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B0%D0%BA%D1%82%D0%B8%D0%B2%D0%BE%D0%B2" +
            "&auctionStartDateTimeFrom=2021-01-01" +
            "&tab=publicTrades" +
            "&propertyAddress=";

    public RtsGrabberService(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Получить информацию по лотам РТС
     *
     * @return кол-во собранных лотов
     */
    public int parse(City city) {
        List<TradingEntity> entities = getRtsLots(city);
        return getRtsLotInfo(entities, city);
    }

    /**
     * Дополнить информацию по каждому лоту с РТС
     *
     * @param entities лоты
     * @param city город
     * @return кол-во собранных лотов
     */
    private int getRtsLotInfo(List<TradingEntity> entities, City city) {
        for (TradingEntity entity : entities) {
            waitFiltered(entity.getUrl(), "openPart");
            SelenideElement address = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvDeliveryAddress_lblValue"));
            address.shouldBe(Condition.visible);
            String addressText = address.text();
            if (!checkCity(addressText, city)) {
                continue;
            }
            SelenideElement description = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_hLotTitle"));
            description.shouldBe(Condition.visible);
            String descriptionText = description.text();
            String price = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvLotPrice_lblValue")).text();
            String auctionStep = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvAuctionStep_lblValue")).text();
            String depositAmount = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvEarnestSum_lblValue")).text();
            String seller = $(By.id("BaseMainContent_MainContent_fvSeller_lblValue")).text();
            entity.setDescription(descriptionText);
            entity.setPrice(BigDecimal.valueOf(Long.parseLong(price.split(",")[0].replaceAll("\\s", ""))));
            entity.setAddress(addressText);
            entity.setAuctionStep(auctionStep);
            entity.setDepositAmount(depositAmount);
            entity.setSeller(seller);
            tradingService.create(entity);
        }
        closeWebDriver();
        return entities.size();
    }

    /**
     * Получить список лотов с сайта РТС
     *
     * @return список лотов
     */
    private List<TradingEntity> getRtsLots(City city) {
        String url = RTS_FILTERED_URL + city.getRegion();
        waitFiltered(url, "lots-table");
        SelenideElement lotsTable = $(By.cssSelector("div.lots-table"));
        List<SelenideElement> elements = lotsTable.$$(By.cssSelector("[data-type-id=proceduresListRow]"));
        String lotNumber = "";
        String lotUrl;
        String acceptRequestsDate = "";
        String tradingTime = "";
        List<TradingEntity> tradingEntities = new ArrayList<>();
        for (SelenideElement el : elements) {
            List<String> fields = Arrays.asList(el.text().split("\n"));
            SelenideElement href = el.$(By.cssSelector("a[data-type-id=proceduresListRowIT1LotNumber]"));
            lotUrl = href.attr("href");
            if (lotUrl != null) {
                lotNumber = lotUrl.split("#")[1];
                lotUrl = lotUrl.replace("http://", "https://");
            }
            if (exists(lotUrl)) {
                continue;
            }
            if (fields.size() > 10) {
                acceptRequestsDate = fields.get(8).concat(" ").concat(fields.get(9));
            }
            if (fields.size() > 11) {
                tradingTime = fields.get(10).concat(" ").concat(fields.get(11));
            }
            TradingEntity tradingEntity = new TradingEntity();
            tradingEntity.setCity(city.getName());
            tradingEntity.setLot(lotNumber);
            tradingEntity.setUrl(lotUrl);
            tradingEntity.setLotSource("РТС");
            tradingEntity.setAcceptRequestsDate(acceptRequestsDate);
            tradingEntity.setTradingTime(tradingTime);
            if (fields.size() > 4) {
                if (!fields.get(4).equalsIgnoreCase("Завершён")) {
                    tradingEntities.add(tradingEntity);
                }
            }
        }
        return tradingEntities;
    }

    /**
     * Метод для открытия и ожидания загрузки страницы
     *
     * @param url адрес страницы
     * @param processName название элемента для ожидания загрузки
     */
    private void waitFiltered(String url, String processName) {
        open(url);
        WebDriverRunner.getWebDriver().manage().window().fullscreen();
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        SelenideElement process = $(By.className(processName));
        process.shouldBe(Condition.visible);
    }

    private boolean exists(String url) {
        return tradingService.existsByUrl(url);
    }

}
