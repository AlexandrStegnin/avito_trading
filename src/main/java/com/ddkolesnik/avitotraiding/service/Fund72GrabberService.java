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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.*;

/**
 * Сервис для сбора информации о торгах с сайта "Фонд имущества Тюменской области" (ФИТО)
 *
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class Fund72GrabberService implements Verifiable {

    private static final String FUND72_URL = "http://www.fund72.ru/torgi/inye-ob-ekty-prodazh";

    private final TradingService tradingService;

    public Fund72GrabberService(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Получить информацию по лотам ФИТО
     *
     * @return кол-во собранных лотов
     */
    public int parse(City city) {
        List<TradingEntity> entities = getFitoLots(city);
        return getFitoLotInfo(entities);
    }

    /**
     * Получить список лотов с сайта ФИТО
     *
     * @return список лотов
     */
    private List<TradingEntity> getFitoLots(City city) {
        Integer lastPage = null;
        int startPos = 0;
        int pageCount = 1;
        List<TradingEntity> tradingEntities = new ArrayList<>();
        while (true) {
            String lotsUrl = FUND72_URL;
            if (pageCount > 1) {
                startPos += 20;
                lotsUrl += "?start=" + startPos;
            }
            waitFiltered(lotsUrl, "pagination");
            SelenideElement lotsTable = getLotsTable();
            if (lotsTable.exists()) {
                List<SelenideElement> blogPosts = lotsTable.$$(By.cssSelector("[itemprop=blogPost]"));
                for (SelenideElement blogPost : blogPosts) {
                    SelenideElement fbPostDesc = blogPost.$(By.cssSelector("div.fbpostdesc"));
                    if (!fbPostDesc.exists()) {
                        fbPostDesc = blogPost.$(By.cssSelector("div.fbpostdesc_full"));
                    }
                    if (fbPostDesc.exists()) {
                        SelenideElement fieldsContainer = fbPostDesc.$(By.cssSelector("dl.fields-container"));
                        if (fieldsContainer.exists()) {
                            int counter = 0;
                            String address = "";
                            StringBuilder acceptRequestsDate = new StringBuilder();
                            List<SelenideElement> fieldsEntry = fieldsContainer.$$(By.cssSelector("dd.field-entry"));
                            if (fieldsEntry.size() >= 5) {
                                for (SelenideElement el : fieldsEntry) {
                                    if (counter == 2) {
                                        SelenideElement addressField = el.$(By.cssSelector("span.field-value"));
                                        if (addressField.exists()) {
                                            address = addressField.text();
                                        }
                                    }
                                    if (counter == 3) {
                                        SelenideElement fromField = el.$(By.cssSelector("span.field-value"));
                                        if (fromField.exists()) {
                                            acceptRequestsDate = new StringBuilder(fromField.text().concat("-"));
                                        }
                                    }
                                    if (counter == 4) {
                                        SelenideElement fromField = el.$(By.cssSelector("span.field-value"));
                                        if (fromField.exists()) {
                                            acceptRequestsDate.append(fromField.text());
                                        }
                                    }
                                    counter++;
                                }
                            }
                            if (!checkCity(address, city)) {
                                continue;
                            }
                            String tradingTime = "";
                            String strPrice = "";
                            SelenideElement fbItemPrice = blogPost.$(By.cssSelector("div.fbtimeprice.clearfix"));
                            if (fbItemPrice.exists()) {
                                List<SelenideElement> fieldValues = fbItemPrice.$$(By.cssSelector("span.field-value"));
                                if (fieldValues.size() == 2) {
                                    tradingTime = fieldValues.get(0).text();
                                    strPrice = fieldValues.get(1).text();
                                }
                            }
                            SelenideElement aHref = blogPost.$(By.cssSelector("a[itemprop=url]"));
                            String url = "";
                            if (aHref.exists()) {
                                url = aHref.attr("href");
                            }
                            String lotSource = "ФИТО";
                            TradingEntity trading = new TradingEntity();
                            trading.setCity(city.getName());
                            trading.setLotSource(lotSource);
                            trading.setUrl(url);
                            trading.setAddress(address);
                            trading.setAcceptRequestsDate(acceptRequestsDate.toString());
                            trading.setTradingTime(tradingTime);
                            BigDecimal price = BigDecimal.ZERO;
                            try {
                                price = BigDecimal.valueOf(Double.parseDouble(strPrice.replaceAll("\\D", "")));
                            } catch (NumberFormatException ignored) {}
                            trading.setPrice(price);
                            tradingEntities.add(trading);
                        }

                    }

                }
                if (lastPage == null) {
                    lastPage = getLastPageNumber();
                }
                if (lastPage == -1 || (pageCount == lastPage)) {
                    break;
                } else {
                    pageCount++;
                }
            }

        }
        return tradingEntities;
    }

    /**
     * Дополнить информацию по каждому лоту с ФИТО
     *
     * @param entities лоты
     * @return кол-во собранных лотов
     */
    private int getFitoLotInfo(List<TradingEntity> entities) {
        for (TradingEntity entity : entities) {
            waitFiltered(entity.getUrl(), "one-blog");
            SelenideElement fbPostDescAr = $(By.cssSelector("div.fbpostdescar"));
            if (fbPostDescAr.exists()) {
                SelenideElement fieldsContainer = fbPostDescAr.$(By.cssSelector("dl.fields-container"));
                if (fieldsContainer.exists()) {
                    List<SelenideElement> fieldsEntry = fieldsContainer.$$(By.cssSelector("dd.field-entry"));
                    if (!fieldsEntry.isEmpty()) {
                        String lotNumber = "";
                        if (fieldsEntry.size() >= 5) {
                            lotNumber = fieldsEntry.get(fieldsEntry.size() - 1).text();
                            entity.setTradingNumber(lotNumber);
                        }
                    }
                }
            }
            SelenideElement fbArctMap = $(By.cssSelector("div.clearfix.fbarctmap"));
            if (fbArctMap.exists()) {
                String description = fbArctMap.$(By.cssSelector("p")).text();
                entity.setDescription(description);
            }
            SelenideElement fbPostSved = $(By.cssSelector("div.fbpostsved"));
            if (fbPostSved.exists()) {
                SelenideElement postSvedFieldContainer = fbPostSved.$(By.cssSelector("dl.fields-container"));
                if (postSvedFieldContainer.exists()) {
                    List<SelenideElement> fieldEntries = postSvedFieldContainer.$$(By.cssSelector("dd.field-entry"));
                    if (!fieldEntries.isEmpty()) {
                        int counter = 0;
                        if (fieldEntries.size() >= 8) {
                            for (SelenideElement element : fieldEntries) {
                                if (counter == 6) {
                                    String depositAmount = element.text();
                                    depositAmount = depositAmount.replaceAll(",", ".").replaceAll("\\D", "");
                                    entity.setDepositAmount(depositAmount);
                                }
                                if (counter == 5) {
                                    String auctionStep = element.text();
                                    auctionStep = auctionStep.replaceAll(",", ".").replaceAll("\\D", "");
                                    entity.setAuctionStep(auctionStep);
                                }
                                counter++;
                            }
                        }
                    }
                }
            }
            SelenideElement headLine = $(By.cssSelector("h2[itemprop=headline]"));
            String lot = "";
            if (headLine.exists()) {
                lot = headLine.text();
            }
            entity.setLot(lot);
            tradingService.create(entity);
        }
        closeWebDriver();
        return entities.size();
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

    private SelenideElement getLotsTable() {
        return $(By.cssSelector("div.mainblog"));
    }

    private int getLastPageNumber() {
        int lastPage = -1;
        // Находим счётчик страниц, если есть
        SelenideElement paginator = $(By.cssSelector("div.pagination"));
        if (paginator.exists()) {
            List<SelenideElement> elements = paginator.findAll(By.cssSelector("li"));
            if (!elements.isEmpty() && elements.size() >= 3) {
                String elText = elements.get(elements.size() - 3).text().split("\n")[0];
                try {
                    lastPage = Integer.parseInt(elText);
                } catch (NumberFormatException ignored) {}
            }
        }
        return lastPage;
    }

}
