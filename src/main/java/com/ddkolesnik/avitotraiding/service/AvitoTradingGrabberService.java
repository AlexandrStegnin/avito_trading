package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.model.TradingEntity;
import com.ddkolesnik.avitotraiding.repository.Grabber;
import com.ddkolesnik.avitotraiding.repository.Verifiable;
import com.ddkolesnik.avitotraiding.utils.City;
import com.ddkolesnik.avitotraiding.utils.Company;
import com.ddkolesnik.avitotraiding.utils.UrlUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

/**
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AvitoTradingGrabberService implements Grabber, Verifiable {

  TradingService tradingService;
  DaDataService daDataService;
  ScraperApiService scraperApiService;

  @Override
  public int parse(Company company, City city) {
    log.info(String.format("Собираем объявления [%s]::[%s]", company.getTitle(), city.getName()));
    String url = UrlUtils.getUrl(company, city);
    log.info(String.format("По ссылке [%s]", url));
    List<String> links = new ArrayList<>();
    int totalPages = getTotalPages(url);
    String pagePart = "&p=";
    int pageNumber = 1;
    while (pageNumber <= totalPages) {
      log.info("Собираем ссылки со страницы {} из {}", pageNumber, totalPages);
      links.addAll(getLinks(url.concat(pagePart).concat(String.valueOf(pageNumber))));
      pageNumber++;
    }
    links = links.stream().distinct().collect(Collectors.toList());
    log.info("Итого собрано ссылок [{} шт]", links.size());
    return getTradings(links, company, city);
  }

  @Override
  public Document getDocument(String url) {
    return scraperApiService.getDocument(url);
  }

  /**
   * Получить кол-во страниц
   *
   * @param url ссылка на страницу
   * @return кол-во страниц
   */
  @Override
  public int getTotalPages(String url) {
    int totalPages;
    try {
      Document document = getDocument(url);
      Element pageCountDiv = document.getElementsByClass("pagination-pages").first();
      if (pageCountDiv != null) {
        Element pageCountHref = pageCountDiv.getElementsByClass("pagination-pages").last();
        if (pageCountHref != null) {
          String pCount = pageCountHref.getElementsByAttribute("href").last()
              .getElementsByAttribute("href").get(0).attr("href")
              .split("=")[1].split("&")[0];
          try {
            totalPages = Integer.parseInt(pCount);
            return totalPages;
          } catch (NumberFormatException e) {
            log.error("Не удалось преобразовать полученный текст [{}] в кол-во объявлений. Ошибка: {}", pCount,
                e.getLocalizedMessage());
            return 0;
          }
        }

      } else {
        return 1;
      }
      return 0;
    } catch (Exception e) {
      log.error(String.format("Произошла ошибка: %s", e.getLocalizedMessage()));
      return 0;
    }
  }

  /**
   * Собрать ссылки на объявления со страницы
   *
   * @param url ссылка на страницу
   * @return список ссылок на объявления
   */
  public List<String> getLinks(String url) {
    List<String> links = new ArrayList<>();
    Document document;
    document = getDocument(url);
    Elements divs = document.select("[data-marker=item]");
    Elements aSnippetLinks = divs.select("a[itemprop=url]");
    for (Element element : aSnippetLinks) {
      String href = element.attr("href");
      if (!href.trim().isEmpty()) {
        links.add(href.trim().replace("?src=bp_catalog", ""));
      }
    }
    return links;
  }

  /**
   * Получить информацию об объявлении со страницы
   *
   * @param url     ссылка на страницу с объявлением
   * @param company компания продавец
   * @param city    город
   */
  public void parseTrading(String url, Company company, City city) {
    url = "http://avito.ru" + url;
    String link = url;

    Document document = getDocument(url);
    String address = getAddress(document);
    if (exists(link)) {
      return;
    }
    if (!checkAddress(address, city)) {
      return;
    }
    String title = getTitle(document);
    if (title == null) {
      return;
    }
    TradingEntity tradingEntity = TradingEntity.builder()
        .lot(title)
        .url(link)
        .price(getPrice(document))
        .address(address)
        .description(getDescription(document))
        .seller(company.getTitle())
        .city(city.getName())
        .area(getArea(document))
        .lotSource("Авито")
        .build();

    daDataService.cleanData(tradingEntity);
    tradingService.create(tradingEntity);
  }

  /**
   * Получить адрес объекта
   *
   * @param document HTML страница
   * @return адрес объекта
   */
  private String getAddress(Document document) {
    String address = null;
    Element addressEl = document.select("span.item-address__string").first();
    if (addressEl != null) {
      address = addressEl.text().trim();
    }
    return address;
  }

  /**
   * Получить описание объявления
   *
   * @param document HTML страница
   * @return описание объявления
   */
  private String getDescription(Document document) {
    String description = "";
    Element descriptionEl = document.selectFirst("div.item-description");
    if (descriptionEl != null) {
      description = descriptionEl.text().trim();
    }
    return description;
  }

  /**
   * Получаем название объявления
   *
   * @param document HTML страница
   * @return название объявления
   */
  private String getTitle(Document document) {
    String title = null;
    Element titleEl = document.select("span.title-info-title-text").first();
    if (titleEl != null) {
      title = titleEl.text();
    }
    return title;
  }

  /**
   * Получить стоимость объекта
   *
   * @param document HTML страница
   * @return стоимость объявления
   */
  private BigDecimal getPrice(Document document) {
    BigDecimal price = BigDecimal.ZERO;
    Element priceEl = document.select("span.js-item-price").select("[itemprop=price]").first();
    if (priceEl != null) {
      String priceStr = priceEl.text().replaceAll("\\s", "");
      price = new BigDecimal(priceStr);
    }
    return price;
  }

  /**
   * Получить список объявлений из массива ссылок
   *
   * @param urls    ссылки на объявления
   * @param company компания продавец
   * @param city    город
   */
  public int getTradings(List<String> urls, Company company, City city) {
    int linksCount = urls.size();
    AtomicInteger counter = new AtomicInteger(0);
    urls.forEach(url -> {
      log.info("Собираем {} из {} объявлений", counter.get() + 1, linksCount);
      parseTrading(url, company, city);
      counter.getAndIncrement();
    });
    return linksCount;
  }

  /**
   * Получаем площадь объявления
   *
   * @param document HTML страница
   * @return площадь объявления
   */
  private String getArea(Document document) {
    String area = null;
    Elements areaEl = document.select("div.item-params");
    if (areaEl != null) {
      Elements areas = areaEl.select("span");
      if (areas.size() == 6) {
        area = areaEl.select("li").text().split(":")[1].replaceAll("[^\\d.]", "");
      } else {
        Element areaFirstEl = areaEl.select("span").first();
        if (areaFirstEl != null) {
          String[] areaParts = areaFirstEl.text().split(":");
          if (areaParts.length > 1) {
            area = areaParts[1].replaceAll("[^\\d.]", "");
          }
        }
      }
    }
    return area;
  }

  /**
   * Проверить адрес, должен содержать в себе Московская область, г Москва/Свердловская обл, г Екатеринбург/Тюменская обл, г Тюмень
   *
   * @param address адресс для проверки
   * @param city    город для получения регулярного выражения
   * @return результат проверки
   */
  private boolean checkAddress(String address, City city) {
    if (address == null) {
      return true;
    }
    if (checkArea(address, city)) {
      return checkCity(address, city);
    }
    return false;
  }

  /**
   * Проверить область по шаблону
   *
   * @param address адрес
   * @param city    город
   * @return результат
   */
  private boolean checkArea(String address, City city) {
    Pattern pattern = Pattern.compile(city.getPattern());
    Matcher matcher = pattern.matcher(address.toLowerCase());
    return matcher.find();
  }

  @Override
  public boolean exists(String url) {
    return tradingService.existsByUrl(url);
  }
}
