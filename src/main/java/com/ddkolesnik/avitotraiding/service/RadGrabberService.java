package com.ddkolesnik.avitotraiding.service;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.ddkolesnik.avitotraiding.model.TradingEntity;
import com.ddkolesnik.avitotraiding.repository.Grabber;
import com.ddkolesnik.avitotraiding.utils.City;
import com.ddkolesnik.avitotraiding.utils.Company;
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class RadGrabberService implements Grabber {

    private final WebClient webClient;

    private final TradingService tradingService;

    public RadGrabberService(WebClient webClient, TradingService tradingService) {
        this.webClient = webClient;
        this.tradingService = tradingService;
    }

    @Override
    public Document getDocument(String url) {
        long timer = 6_000;
        try {
            Thread.sleep(timer);
        } catch (InterruptedException e) {
            log.error("Произошла ошибка: " + e.getLocalizedMessage());
        }
        HtmlPage page;
        try {
            webClient.setAjaxController(new AjaxController(){
                @Override
                public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
                    return false;
                }
            });
            page = webClient.getPage(url);
            for (int i = 0; i < 20; i++) {
                if (page.asXml().contains("div[id=container-filter]")) {
                    break;
                }
                synchronized (page) {
                    page.wait(500);
                }
            }
            return Jsoup.parse(page.asXml());
        }  catch (HttpStatusException e) {
            waiting(e);
        } catch (Exception e) {
            log.error("Произошла ошибка: " + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public int getTotalPages(String url) {
        return 0;
    }

    @Override
    public int parse(Company company, City city) {
        Map<String, String> links = new HashMap<>();
        try {
            links = getLinks(city);
            List<TradingEntity> entities = new ArrayList<>();
            links.forEach((lot, url) -> entities.add(getAuctionInfo(lot, url, city)));
            return entities.size();
        } catch (Exception e) {
            log.error("Произошла ошибка: {}", e.getLocalizedMessage());
        }
        return links.size();
    }

    /**
     * Получить список ссылок со страницы
     *
     * @return список ссылок и лотов
     */
    private Map<String, String> getLinks(City city) {
        Map<String, String> links = new HashMap<>();
        filterPage(city);
        while (true) {
            links.putAll(prepareLinks());
            SelenideElement nextPage = getNextPage();
            if (nextPage.exists()) {
                nextPage.click();
                try {
                    Thread.sleep(3_000);
                } catch (InterruptedException e) {
                    log.error("Произошла ошибка: {}", e.getLocalizedMessage());
                }
            } else {
                break;
            }
        }
        return links;
    }

    /**
     * Отфильтровать страницу в зависимости от города
     *
     * @param city город
     */
    private void filterPage(City city) {
        open("https://sales.lot-online.ru/e-auction/lots.xhtml");
        WebDriverRunner.getWebDriver().manage().window().fullscreen();
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        // Раскрываем форму поиска
        $(By.id("formMain:switcher-filter")).click();
        // Берём элемент формы "Регион"
        SelenideElement region = $(By.id("formMain:scmSubjectRFId"));
        // Нажимаем треугольник, чтоб раскрылся список
        SelenideElement btnRF = region.find(By.cssSelector("div.ui-selectcheckboxmenu-trigger.ui-state-default.ui-corner-right"));
        btnRF.click();

        // Берём элемент раскрывшегося списка (панель)
        SelenideElement rfPanel = $(By.id("formMain:scmSubjectRFId_panel"));
        // Находим лэйбл, на котором содержится "Тюменская" и кликаем по нему
        SelenideElement rfLabel = rfPanel.find(withText(city.getRegion()));
        rfLabel.click();

        // Находим крестик и закрываем выпадающий список
        rfPanel.find(By.cssSelector("span.ui-icon.ui-icon-circle-close")).click();

        // Находим текстовое поле для ключевых слов вводим туда текст
        SelenideElement keyWordsInput = $(By.id("formMain:itKeyWords"));
        keyWordsInput.setValue(city.getName());

        // Находим поле время проведения "C" и вставляем текущую дату
        SelenideElement dateFrom = $(By.id("formMain:auctionDatePlanBID_input"));
        LocalDate now = LocalDate.now();
        dateFrom.setValue(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        // Находим поле время проведения "ПО" и вставляем текущую дату
        SelenideElement dateTo = $(By.id("formMain:auctionDatePlanEID_input"));
        LocalDate after = now.plusMonths(6);
        dateTo.setValue(after.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        // Находим переключатель "Активные торги" и кликаем
        SelenideElement radioActive = $(By.id("formMain:selectIndPublish:0"));
        radioActive.click();

        // Находим кнопку "Искать" и кликаем по ней
        SelenideElement submitFilter = $(By.id("formMain:cbFilter"));
        submitFilter.click();

        // Находим окно процесса загрузки и пока оно активно - ждём
        SelenideElement process = $(By.id("procId"));
        process.shouldNotBe(Condition.visible);
    }

    /**
     * Достать список лотов и ссылок со страницы
     *
     * @return список лотов и ссылок на них
     */
    private Map<String, String> prepareLinks() {
        Map<String, String> linksMap = new HashMap<>();
        // Находим таблицу с объявлениями
        SelenideElement contTender = $(By.cssSelector("div.cont-tender"));
        // Находим переключатель представлений (интересует табличное представление)
        SelenideElement spanViewTable = contTender.find(By.id("formMain:formSelectTableType"));

        // Находим переключатель представления в табличный вид и кликаем его
        SelenideElement tableView = spanViewTable.find(By.cssSelector("li.view-table"));
        tableView.click();
        tableView.shouldHave(Condition.cssClass("active"));

        // Находим объект таблицу на странице
        SelenideElement table = $(By.id("formMain:auctionLotCategoryTable"));

        // Находим все строки таблицы на странице
        List<SelenideElement> rows = table.$$("tbody tr");
        rows.forEach(row -> {
            SelenideElement lot = row.find(By.cssSelector("span.field.field-lot"));
            if (lot != null) {
                SelenideElement aHref = row.find(By.cssSelector("a.command-link-text"));
                if (aHref != null) {
                    linksMap.put(lot.text(), aHref.attr("href"));
                }
            }
        });

        return linksMap;
    }

    /**
     * Получить переключатель следующей страницы
     *
     * @return элемент
     */
    private SelenideElement getNextPage() {
        // Находим счётчик страниц, если есть
        SelenideElement paginator = $(By.id("formMain:LotListPaginatorID"));
        return paginator.find(By.cssSelector("span.item.next"));
    }

    /**
     * Собрать информацию об аукционе со страницы
     *
     * @param lot название лота
     * @param url ссылка
     * @param city город
     * @return информация об аукционе
     */
    private TradingEntity getAuctionInfo(String lot, String url, City city) {
        Document document = getDocument(url);
        TradingEntity tradingEntity = new TradingEntity();
        tradingEntity.setLot(lot);
        tradingEntity.setUrl(url);
        tradingEntity.setDescription(getDescription(document));
        tradingEntity.setAddress(getAddress(document));
        tradingEntity.setTradingNumber(getTradingNumber(document));
        tradingEntity.setAuctionStep(getAuctionStep(document));
        tradingEntity.setEfrsbId(getEfrsbId(document));
        tradingEntity.setDepositAmount(getDepositAmount(document));
        tradingEntity.setTradingTime(getTradingTime(document));
        tradingEntity.setAcceptRequestsDate(getAcceptRequestsDate(document));
        tradingEntity.setLotSource("Российский Аукционный Дом");
        tradingEntity.setCity(city.getName());
        return tradingService.create(tradingEntity);
    }

    /**
     * Получить элемент страницы
     *
     * @param document страница-документ
     * @param cssSelector селектор
     * @return элемент
     */
    private Element getElement(Document document, String cssSelector) {
        return document.selectFirst(cssSelector);
    }

    /**
     * Получить описание аукциона
     *
     * @param document документ-страница
     * @return описание аукциона
     */
    private String getDescription(Document document) {
        Element product = getElement(document, "div.product");
        List<Element> paragraphs = product.select("p");
        StringBuilder description = new StringBuilder();
        int counter = 0;
        for (Element p : paragraphs) {
            counter ++;
            if (counter == 1 || counter == 3) {
                description.append(p.text()).append(" ");
            }
        }
        return description.toString().trim();
    }

    /**
     * Получить адрес аукциона
     *
     * @param document документ-страница
     * @return адрес аукциона
     */
    private String getAddress(Document document) {
        Element product = getElement(document, "div.product");
        List<Element> paragraphs = product.select("p");
        StringBuilder address = new StringBuilder();
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 4) {
                address.append(p.text());
            }
        }
        return address.toString().trim();
    }

    /**
     * Получить номер аукциона
     *
     * @param document документ-страница
     * @return номер аукциона
     */
    private String getTradingNumber(Document document) {
        String tradingNumber = "";
        Element tender = getElement(document, "div.tender");
        List<Element> paragraphs = tender.select("p");
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 7) {
                tradingNumber = p.text().replaceAll("\\D", "");
                break;
            }
        }
        return tradingNumber;
    }

    /**
     * Получить идентификационный номер ЕФРСБ аукциона
     *
     * @param document документ-страница
     * @return идентификационный номер ЕФРСБ аукциона
     */
    private String getEfrsbId(Document document) {
        String efrsbId = "";
        Element tender = getElement(document, "div.tender");
        List<Element> paragraphs = tender.select("p");
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 8) {
                efrsbId = p.text().replaceAll("\\D", "");
                break;
            }
        }
        return efrsbId;
    }

    /**
     * Получить шаг аукциона
     *
     * @param document документ-страница
     * @return шаг аукциона
     */
    private String getAuctionStep(Document document) {
        String auctionStep = "";
        Element tender = getElement(document, "div.tender");
        Element step = tender.selectFirst("div[id=formMain:opStepValue]");
        if (step != null) {
            List<Element> paragraphs = step.select("span.gray1");
            if (paragraphs.size() >0) {
                auctionStep = paragraphs.get(0).text().replaceAll("\\D", "");
            }
        }
        return auctionStep;
    }

    /**
     * Получить сумму задатка аукциона
     *
     * @param document документ-страница
     * @return сумма задатка аукциона
     */
    private String getDepositAmount(Document document) {
        String depositAmount = "";
        Element tender = getElement(document, "div.tender");
        List<Element> spans = tender.select("span.gray1");
        int counter = 0;
        for (Element span : spans) {
            counter++;
            if (counter == 6) {
                depositAmount = span.text().replaceAll("\\s", "");
            }
        }
        return depositAmount;
    }

    /**
     * Получить время проведения аукциона
     *
     * @param document документ-страница
     * @return время проведения аукциона
     */
    private String getTradingTime(Document document) {
        String tradingTime;
        Element tender = getElement(document, "div.tender");
        Element paragraph = tender.selectFirst("p");
        Element em = paragraph.selectFirst("em");
        tradingTime = em.text().substring(0, 18);
        return tradingTime;
    }

    /**
     * Получить время приёма заявок по аукциону
     *
     * @param document документ-страница
     * @return время приёма заявок по аукциону
     */
    private String getAcceptRequestsDate(Document document) {
        StringBuilder acceptRequestDate = new StringBuilder();
        Element tender = getElement(document, "div.tender");
        List<Element> paragraphs = tender.select("p");
        int counter = 0;
        for (Element p : paragraphs) {
            counter++;
            if (counter == 2) {
                Element em = p.selectFirst("em");
                if (em != null) {
                    List<Element> spans = em.select("span.gray1");
                    for (Element span : spans) {
                        acceptRequestDate.append(span.text()).append("\n");
                    }
                }
            }
        }
        return acceptRequestDate.toString().trim();
    }

    /**
     * Метод для ожидания, в случае, если сервер сказал, что мы "спамим"
     *
     * @param e ошибка
     */
    private void waiting(HttpStatusException e) {
        if (e.getStatusCode() == 429) {
            log.error("Слишком много запросов {}", e.getLocalizedMessage());
            log.info("Засыпаем на 60 мин для обхода блокировки");
            try {
                Thread.sleep(60 * 1000 * 60);
            } catch (InterruptedException exception) {
                log.error(String.format("Произошла ошибка: [%s]", exception));
            }
        }
    }

}
