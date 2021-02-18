package com.ddkolesnik.avitotraiding.service;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.avitotraiding.model.TradingEntity;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Севрис для получения информации о торгах с площадки РТС
 *
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class RtsGrabberService {

    private final TradingService tradingService;

    private final static String RTS_FILTERED_URL = "https://it2.rts-tender.ru/?priceFrom=1000000" +
            "&procedureTypeNames=%D0%9F%D1%80%D0%B8%D0%B2%D0%B0%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%20%D0%BC%D1%83%D0%BD%D0%B8%D1%86%D0%B8%D0%BF%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B0%D1%80%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%B0%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0,%20%D0%BE%D0%B1%D1%80%D0%B0%D1%89%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B2%20%D1%81%D0%BE%D0%B1%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D1%81%D1%82%D1%8C%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B0" +
            "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20(%D0%BF%D1%80%D0%BE%D0%B4%D0%B0%D0%B6%D0%B0)%20%D0%BD%D0%B5%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%20%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B0%D0%BA%D1%82%D0%B8%D0%B2%D0%BE%D0%B2" +
            "&propertyAddress=%D0%A2%D1%8E%D0%BC%D0%B5%D0%BD%D1%81%D0%BA%D0%B0%D1%8F" +
            "&auctionStartDateTimeFrom=2021-01-01" +
            "&tab=publicTrades";

    public RtsGrabberService(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Получить информацию по лотам РТС
     *
     * @return кол-во собранных лотов
     */
    public int parse() {
        List<TradingEntity> entities = getRtsLots();
        return getRtsLotInfo(entities);
    }

    /**
     * Дополнить информацию по каждому лоту с РТС
     *
     * @param entities лоты
     * @return кол-во собранных лотов
     */
    private int getRtsLotInfo(List<TradingEntity> entities) {
        for (TradingEntity entity : entities) {
            waitFiltered(entity.getUrl(), "openPart");
            String description = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvName_lblValue")).text();
            String price = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvLotPrice_lblValue")).text();
            String address = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvDeliveryAddress_lblValue")).text();
            String auctionStep = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvAuctionStep_lblValue")).text();
            String depositAmount = $(By.id("BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvEarnestSum_lblValue")).text();
            String seller = $(By.id("BaseMainContent_MainContent_fvSeller_lblValue")).text();
            entity.setDescription(description);
            entity.setPrice(BigDecimal.valueOf(Long.parseLong(price.split(",")[0])));
            entity.setAddress(address);
            entity.setAuctionStep(auctionStep);
            entity.setDepositAmount(depositAmount);
            entity.setSeller(seller);
            tradingService.create(entity);
        }
        return entities.size();
    }

    /**
     * Получить список лотов с сайта РТС
     *
     * @return список лотов
     */
    private List<TradingEntity> getRtsLots() {
        waitFiltered(RTS_FILTERED_URL, "lots-table");
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
            }
            if (fields.size() > 10) {
                acceptRequestsDate = fields.get(8).concat(" ").concat(fields.get(9));
            }
            if (fields.size() > 11) {
                tradingTime = fields.get(10).concat(" ").concat(fields.get(11));
            }
            TradingEntity tradingEntity = new TradingEntity();
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
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        SelenideElement process = $(By.className(processName));
        process.shouldBe(Condition.visible);
    }
}
