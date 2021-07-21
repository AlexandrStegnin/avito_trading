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
 * Севрис для получения информации о торгах с площадки РТС
 *
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RtsGrabberService implements Verifiable {

  TradingService tradingService;
  ScraperApiService scraperApiService;

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
   * Получить список лотов с сайта РТС
   *
   * @return список лотов
   */
  private List<TradingEntity> getRtsLots(City city) {
    String rtsFilteredUrl = "https://it2.rts-tender.ru/?priceFrom=1000000" +
        "&procedureTypeNames=%D0%9F%D1%80%D0%B8%D0%B2%D0%B0%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%20%D0%BC%D1%83%D0%BD%D0%B8%D1%86%D0%B8%D0%BF%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
        "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B0%D1%80%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%B0%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0" +
        "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B8%D0%BC%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D0%B0,%20%D0%BE%D0%B1%D1%80%D0%B0%D1%89%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D0%B2%20%D1%81%D0%BE%D0%B1%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D1%81%D1%82%D1%8C%20%D0%B3%D0%BE%D1%81%D1%83%D0%B4%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%B0" +
        "&procedureTypeNames=%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20(%D0%BF%D1%80%D0%BE%D0%B4%D0%B0%D0%B6%D0%B0)%20%D0%BD%D0%B5%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%20%D0%BF%D1%80%D0%BE%D1%84%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B0%D0%BA%D1%82%D0%B8%D0%B2%D0%BE%D0%B2" +
        "&auctionStartDateTimeFrom=2021-01-01" +
        "&tab=publicTrades" +
        "&propertyAddress=";
    String url = rtsFilteredUrl + city.getRegion();
    Document document = getDocument(url);
    Element lotsTable = document.select("div.lots-table").first();
    List<TradingEntity> tradingEntities = new ArrayList<>();
    if (Objects.nonNull(lotsTable)) {
      List<Element> elements = lotsTable.select("[data-type-id=proceduresListRow]");
      String lotNumber = "";
      String lotUrl = null;

      for (Element el : elements) {
        List<Element> divs = el.select("div");
        Element href = el.selectFirst("a[data-type-id=proceduresListRowIT1LotNumber]");
        if (Objects.nonNull(href)) {
          lotUrl = href.attr("href");
        }
        if (lotUrl != null) {
          lotNumber = lotUrl.split("#")[1];
          lotUrl = lotUrl.replace("http://", "https://");
        }
        if (exists(lotUrl)) {
          continue;
        }
        TradingEntity tradingEntity = TradingEntity.builder()
            .city(city.getName())
            .lot(lotNumber)
            .url(lotUrl)
            .lotSource("РТС")
            .build();

        divs.stream()
            .skip(6)
            .findFirst()
            .ifPresent(div -> {
              String text = div.text();
              tradingEntity.setAcceptRequestsDate(text.substring(0, 10).concat(" ").concat(text.substring(11)));
            });

        divs.stream()
            .skip(7)
            .findFirst()
            .ifPresent(div -> {
              String text = divs.get(7).text();
              tradingEntity.setTradingTime(text.substring(0, 10).concat(" ").concat(text.substring(11)));
            });

        divs.stream()
            .skip(3)
            .filter(div -> !div.text().equalsIgnoreCase("Завершён"))
            .findFirst()
            .ifPresent(div -> tradingEntities.add(tradingEntity));
      }
    }
    return tradingEntities;
  }

  /**
   * Дополнить информацию по каждому лоту с РТС
   *
   * @param entities лоты
   * @param city     город
   * @return кол-во собранных лотов
   */
  private int getRtsLotInfo(List<TradingEntity> entities, City city) {
    for (TradingEntity entity : entities) {
      Document document = getDocument(entity.getUrl());
      Element address =
          document.selectFirst("span#BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvDeliveryAddress_lblValue");
      if (Objects.nonNull(address)) {
        String addressText = address.text();
        if (!checkCity(addressText, city)) {
          continue;
        }
        entity.setAddress(addressText);
      }
      Element description = document.selectFirst("h2#BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_hLotTitle");
      if (Objects.nonNull(description)) {
        entity.setDescription(description.text());
      }
      Element price = document.selectFirst("span#BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvLotPrice_lblValue");
      if (Objects.nonNull(price)) {
        String priceStr = price.text();
        BigDecimal priceValue = BigDecimal.ZERO;
        try {
          priceValue = BigDecimal.valueOf(Long.parseLong(priceStr.split(",")[0].replaceAll("\\s", "")));
        } catch (NumberFormatException ignored) {
        }
        entity.setPrice(priceValue);
      }
      Element auctionStep =
          document.selectFirst("span#BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvAuctionStep_lblValue");
      if (Objects.nonNull(auctionStep)) {
        entity.setAuctionStep(auctionStep.text());
      }
      Element depositAmount =
          document.selectFirst("span#BaseMainContent_MainContent_ucTradeLotViewList_tlvLot_fvEarnestSum_lblValue");
      if (Objects.nonNull(depositAmount)) {
        entity.setDepositAmount(depositAmount.text());
      }
      Element seller = document.selectFirst("span#BaseMainContent_MainContent_fvSeller_lblValue");
      if (Objects.nonNull(seller)) {
        entity.setSeller(seller.text());
      }
      tradingService.create(entity);
    }
    return entities.size();
  }

  private boolean exists(String url) {
    return tradingService.existsByUrl(url);
  }

  private Document getDocument(String url) {
    return scraperApiService.getDocument(url);
  }

}
