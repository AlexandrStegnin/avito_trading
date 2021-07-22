package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.model.TradingEntity;
import com.ddkolesnik.avitotraiding.repository.Verifiable;
import com.ddkolesnik.avitotraiding.utils.City;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Сервис для сбора информации о торгах с сайта "Фонд имущества Тюменской области" (ФИТО)
 *
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Fund72GrabberService implements Verifiable {

  static String FUND72_URL = "http://www.fund72.ru/torgi/inye-ob-ekty-prodazh";

  TradingService tradingService;
  ScraperApiService scraperApiService;

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
      Document document = getDocument(lotsUrl);
      Element lotsTable = document.selectFirst("div.mainblog");
      if (Objects.nonNull(lotsTable)) {
        List<Element> blogPosts = lotsTable.select("[itemprop=blogPost]");
        for (Element blogPost : blogPosts) {
          Element fbPostDesc = blogPost.selectFirst("div.fbpostdesc");
          if (Objects.isNull(fbPostDesc)) {
            fbPostDesc = blogPost.selectFirst("div.fbpostdesc_full");
          }
          if (Objects.nonNull(fbPostDesc)) {
            Element fieldsContainer = fbPostDesc.selectFirst("dl.fields-container");
            if (Objects.nonNull(fieldsContainer)) {
              int counter = 0;
              String address = "";
              StringBuilder acceptRequestsDate = new StringBuilder();
              List<Element> fieldsEntry = fieldsContainer.select("dd.field-entry");
              if (fieldsEntry.size() >= 5) {
                for (Element el : fieldsEntry) {
                  if (counter == 2) {
                    Element addressField = el.selectFirst("span.field-value");
                    if (Objects.nonNull(addressField)) {
                      address = addressField.text();
                    }
                  }
                  if (counter == 3) {
                    Element fromField = el.selectFirst("span.field-value");
                    if (Objects.nonNull(fromField)) {
                      acceptRequestsDate = new StringBuilder(fromField.text().concat("-"));
                    }
                  }
                  if (counter == 4) {
                    Element fromField = el.selectFirst("span.field-value");
                    if (Objects.nonNull(fromField)) {
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
              Element fbItemPrice = blogPost.selectFirst("div.fbtimeprice.clearfix");
              if (Objects.nonNull(fbItemPrice)) {
                List<Element> fieldValues = fbItemPrice.select("span.field-value");
                if (fieldValues.size() == 2) {
                  tradingTime = fieldValues.get(0).text();
                  strPrice = fieldValues.get(1).text();
                }
              }
              Element aHref = blogPost.selectFirst("a[itemprop=url]");
              String url = "";
              if (Objects.nonNull(aHref)) {
                url = aHref.attr("href");
              }
              if (exists(url)) {
                continue;
              }

              BigDecimal price = BigDecimal.ZERO;
              try {
                price = BigDecimal.valueOf(Double.parseDouble(strPrice.replaceAll("\\D", "")));
              } catch (NumberFormatException ignored) {
              }

              TradingEntity trading = TradingEntity.builder()
                  .city(city.getName())
                  .lotSource("ФИТО")
                  .url(url)
                  .address(address)
                  .acceptRequestsDate(acceptRequestsDate.toString())
                  .tradingTime(tradingTime)
                  .price(price)
                  .build();

              tradingEntities.add(trading);
            }
          }
        }
        if (lastPage == null) {
          lastPage = getLastPageNumber(document);
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
      String url = FUND72_URL + entity.getUrl();
      Document document = getDocument(url);
      Element fbPostDescAr = document.selectFirst("div.fbpostdescar");
      if (Objects.nonNull(fbPostDescAr)) {
        Element fieldsContainer = fbPostDescAr.selectFirst("dl.fields-container");
        if (Objects.nonNull(fieldsContainer)) {
          List<Element> fieldsEntry = fieldsContainer.select("dd.field-entry");
          fieldsEntry.stream()
              .skip(4)
              .findFirst()
              .ifPresent(field -> {
                Element fieldEl = field.selectFirst("span.field-value");
                if (Objects.nonNull(fieldEl)) {
                  String lotNumber = fieldEl.text();
                  entity.setTradingNumber(lotNumber);
                }
              });
        }
      }
      Element fbArctMap = document.selectFirst("div.clearfix.fbarctmap");
      if (Objects.nonNull(fbArctMap)) {
        Element description = fbArctMap.selectFirst("p");
        if (Objects.nonNull(description)) {
          entity.setDescription(description.text());
        }
      }
      Element fbPostSved = document.selectFirst("div.fbpostsved");
      if (Objects.nonNull(fbPostSved)) {
        Element postSvedFieldContainer = fbPostSved.selectFirst("dl.fields-container");
        if (Objects.nonNull(postSvedFieldContainer)) {
          List<Element> fieldEntries = postSvedFieldContainer.select("dd.field-entry");

          fieldEntries.stream()
              .skip(5)
              .findFirst()
              .ifPresent(field -> {
                String auctionStep = field.text();
                auctionStep = auctionStep.replaceAll(",", ".").replaceAll("\\D", "");
                entity.setAuctionStep(auctionStep);
              });

          fieldEntries.stream()
              .skip(6)
              .findFirst()
              .ifPresent(field -> {
                String depositAmount = field.text();
                depositAmount = depositAmount.replaceAll(",", ".").replaceAll("\\D", "");
                entity.setDepositAmount(depositAmount);
              });

        }
      }
      Element headLine = document.selectFirst("h2[itemprop=headline]");
      if (Objects.nonNull(headLine)) {
        entity.setLot(headLine.text());
      }
      tradingService.create(entity);
    }
    return entities.size();
  }

  private int getLastPageNumber(Document document) {
    int lastPage = -1;
    // Находим счётчик страниц, если есть
    Element paginator = document.selectFirst("div.pagination");
    if (Objects.nonNull(paginator)) {
      List<Element> elements = paginator.select("li");
      if (!elements.isEmpty() && elements.size() >= 3) {
        String elText = elements.get(elements.size() - 3).text().split("\n")[0];
        try {
          lastPage = Integer.parseInt(elText);
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return lastPage;
  }

  private boolean exists(String url) {
    return tradingService.existsByUrl(url);
  }

  private Document getDocument(String url) {
    return scraperApiService.getDocument(url);
  }
}
