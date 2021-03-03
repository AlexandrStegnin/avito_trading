package com.ddkolesnik.avitotraiding.service;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.avitotraiding.model.TradingEntity;
import com.ddkolesnik.avitotraiding.utils.City;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.*;

/**
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class SberAstGrabberService {

    private static final String DEFAULT_URL = "https://utp.sberbank-ast.ru/Main/List/UnitedPurchaseListNew";

    private final TradingService tradingService;

    public SberAstGrabberService(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Получить информацию по лотам Сбер АСТ
     *
     * @return кол-во собранных лотов
     */
    public int parse(City city) {
        List<TradingEntity> entities = getSberAstLots(city);
        return entities.size();
    }

    /**
     * Получить список лотов с сайта Сбер АСТ
     *
     * @return список лотов
     */
    private List<TradingEntity> getSberAstLots(City city) {
        List<TradingEntity> tradingEntities = new ArrayList<>();
        waitFiltered(DEFAULT_URL);
        openFilterForm();
        fillFilterCriteria(city);
        show100Items();
        List<SelenideElement> links = collectLinks();
        for (SelenideElement link : links) {
            tradingEntities.add(getPageData(link, city));
        }
        return tradingEntities;
    }

    private TradingEntity getPageData(SelenideElement link, City city) {
        link.click();
        Set<String> windowHandles = WebDriverRunner.getWebDriver().getWindowHandles();
        String newUri = windowHandles.toArray()[windowHandles.size() - 1].toString();
        String firstUri = WebDriverRunner.getWebDriver().getWindowHandles().toArray()[0].toString();
        WebDriverRunner.getWebDriver().switchTo().window(newUri);
        $(By.cssSelector("div[id=dataPanel]")).should(Condition.visible);
        TradingEntity tradingEntity = new TradingEntity();
        tradingEntity.setUrl(WebDriverRunner.url());
        SelenideElement lotSpan = $(By.name("DynamicControlPurchaseInfo_PurchaseCode"));
        lotSpan.should(Condition.exist);
        tradingEntity.setLot(lotSpan.text());
        SelenideElement descriptionSpan = $(By.cssSelector("span[id=DynamicControlPurchaseInfo_PurchaseName]"));
        descriptionSpan.should(Condition.exist);
        tradingEntity.setDescription(descriptionSpan.text());
        tradingEntity.setAddress("");
        tradingEntity.setTradingNumber("");
        SelenideElement idEfrsb = $(By.cssSelector("span[id=DynamicControlPurchaseInfo_IDEFRSB]"));
        idEfrsb.should(Condition.exist);
        tradingEntity.setEfrsbId(idEfrsb.text());
        tradingEntity.setAuctionStep("");
        SelenideElement depositSpan = $(By.name("DynamicControlBidsInfo_BidPrice"));
        depositSpan.should(Condition.exist);
        tradingEntity.setDepositAmount(depositSpan.text());

        SelenideElement tradingTimeSpan = $(By.name("DynamicControlTerms_PurchaseAuctionStartDate"));
        if (!tradingTimeSpan.exists()) {
            tradingTimeSpan = $(By.name("DynamicControlResultInfo_AuctionResultDate"));
        }
        tradingEntity.setTradingTime(tradingTimeSpan.text());

        SelenideElement startDateSpan = $(By.name("DynamicControlRequestInfo_RequestStartDate"));
        startDateSpan.should(Condition.exist);
        String acceptRequestsDate = startDateSpan.text().concat(" ");
        SelenideElement endDateSpan = $(By.name("DynamicControlRequestInfo_RequestStopDate"));
        endDateSpan.should(Condition.exist);
        acceptRequestsDate = acceptRequestsDate.concat(endDateSpan.text());
        tradingEntity.setAcceptRequestsDate(acceptRequestsDate);

        SelenideElement priceSpan = $(By.name("DynamicControlBidsInfo_BidPrice"));
        priceSpan.should(Condition.exist);
        String strPrice = priceSpan.text();
        BigDecimal price = BigDecimal.ZERO;
        try {
            price = BigDecimal.valueOf(Long.parseLong(strPrice.split("\\.")[0]));
        } catch (NumberFormatException ignored) {}
        tradingEntity.setPrice(price);
        tradingEntity.setSeller("");
        tradingEntity.setCity(city.getName());
        tradingEntity.setLotSource("Сбербанк АСТ");
        WebDriverRunner.getWebDriver().switchTo().window(firstUri);
        return tradingService.create(tradingEntity);
    }

    /**
     * Собрать ссылки для последующей обработки
     */
    private List<SelenideElement> collectLinks() {
        SelenideElement resultTable = $(By.cssSelector("div[id=resultTable]"));
        resultTable.should(Condition.exist);
        List<SelenideElement> resultTbl = resultTable.$$(By.cssSelector("table.es-reestr-tbl.its"));
        return resultTbl
                .stream()
                .map(se -> se.$(By.cssSelector("a.link-button.STRView")))
                .collect(Collectors.toList());
    }

    /**
     * Переключиться на показ 100 записей на странице
     */
    private void show100Items() {
        SelenideElement pagerSelect = $(By.cssSelector("select[id=headerPagerSelect]"));
        pagerSelect.should(Condition.exist);
        pagerSelect.selectOption("100");
        waiting();
    }

    /**
     * Отфильтровать страницу по заданным параметрам
     *
     * @param city город
     */
    private void fillFilterCriteria(City city) {
        SelenideElement filterTable = $(By.cssSelector("table.filter-table"));
        filterTable.should(Condition.exist);
        filterByKeywords(city.getName());
        filterBy("Торговая секция", "Продажа имущества (предприятия) банкротов");
        filterBy("Регион", city.getRegion() + " область");
        filterByPrice();
        SelenideElement filterForm = $(By.cssSelector("form[id=filterForm]"));
        filterForm.should(Condition.exist);
        SelenideElement okCancel = filterForm.$(By.cssSelector("div[id=OkCansellBtns]"));
        okCancel.should(Condition.exist);
        okCancel.find("input[type=button][value=Поиск]").click();
        waiting();
    }

    /**
     * Вставить фильтрацию по сумме (500 000 тыс руб)
     */
    private void filterByPrice() {
        String esFilterName = String.format("[esfiltername='%s']", "Начальная цена");
        SelenideElement priceSectionRow = $(By.cssSelector(esFilterName));
        priceSectionRow.should(Condition.exist);
        SelenideElement input = $(By.cssSelector("input[type=text]"));
        input.should(Condition.exist);
        input.setValue("500000");
    }

    /**
     * Отфильтровать по торговой секции/региону
     *
     * @param filterName название фильтра
     * @param foundText название секции/региона
     */
    private void filterBy(String filterName, String foundText) {
        String esFilterName = String.format("[esfiltername='%s']", filterName);
        SelenideElement tradingSectionRow = $(By.cssSelector(esFilterName));
        tradingSectionRow.should(Condition.exist);
        SelenideElement chooseSection = tradingSectionRow.$(By.cssSelector("input.shortdict-filter-choose-button"));
        chooseSection.should(Condition.exist);
        chooseSection.click();
        SelenideElement shortDictionaryModal = $(By.cssSelector("div[id=shortDictionaryModal]"));
        shortDictionaryModal.should(Condition.cssValue("display", "block"));
        SelenideElement section = shortDictionaryModal.find(new Selectors.ByText(foundText));
        section.should(Condition.exist);
        section.parent().parent().find("[type=checkbox]").click();
        SelenideElement modalFooter = shortDictionaryModal.$(By.cssSelector("div.modal-footer"));
        modalFooter.should(Condition.exist);
        modalFooter.find("input[type=button]").click();
        waiting();
    }

    /**
     * Отфильтровать по ключевому слову
     *
     * @param keyword ключевое слово
     */
    private void filterByKeywords(String keyword) {
        SelenideElement mainSearchBar = $(By.cssSelector("div.mainSearchBar-containter"));
        mainSearchBar.should(Condition.exist);
        SelenideElement searchInput = mainSearchBar.$(By.cssSelector("input[id=searchInput]"));
        searchInput.should(Condition.exist);
        searchInput.setValue(keyword);
        SelenideElement searchBtn = mainSearchBar.$(By.cssSelector("button.mainSearchBar-find"));
        searchBtn.should(Condition.exist);
        searchBtn.click();
        waiting();
    }

    /**
     * Раскрыть форму фильтрации
     */
    private void openFilterForm() {
        SelenideElement btn = $(By.cssSelector("button.element-in-one-row.simple-button.orange-background"));
        btn.shouldBe(Condition.visible);
        btn.click();
    }

    /**
     * Метод для открытия и ожидания загрузки страницы
     *
     * @param url адрес страницы
     */
    private void waitFiltered(String url) {
        open(url);
        WebDriverRunner.getWebDriver().manage().window().fullscreen();
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        waiting();
    }

    /**
     * Ожидаем, когда скроется прогресс-бар
     */
    private void waiting() {
        SelenideElement ajaxBackground = $(By.cssSelector("div[id=ajax-background]"));
        ajaxBackground.should(Condition.cssValue("display", "none"));
    }

}
