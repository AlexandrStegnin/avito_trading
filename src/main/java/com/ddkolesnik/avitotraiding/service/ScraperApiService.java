package com.ddkolesnik.avitotraiding.service;

import com.scraperapi.ScraperApiClient;
import kong.unirest.Unirest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * @author Aleksandr Stegnin on 21.07.2021
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ScraperApiService {

  ScraperApiClient client;

  public Document getDocument(String url) {
    reset();
    return Jsoup.parse(client.get(url).timeout(0).render(true).result());
  }

  public void reset() {
    Unirest.config().reset();
  }

}
