package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.repository.Grabber;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Alexandr Stegnin
 */

@Slf4j
@Service
public class AvitoTradingGrabberService implements Grabber {

    private final Map<String, String> cookieMap = new HashMap<>();

    @Override
    public int parse(String company, String city) {
        return 0;
    }

    @Override
    public Document getDocument(String url) throws IOException {
        long timer = 6_000;
        try {
            log.info(String.format("Засыпаем на %d секунд", (timer / 1000)));
            Thread.sleep(timer);
        } catch (InterruptedException e) {
            log.error("Произошла ошибка: " + e.getLocalizedMessage());
        }
        int number = ThreadLocalRandom.current().nextInt(0, 1);
        Connection.Response response = Jsoup.connect(url)
                .userAgent(switchUserAgent(number))
                .referrer(url)
                .cookies(cookieMap)
                .method(Connection.Method.GET)
                .execute();

        cookieMap.clear();
        cookieMap.putAll(response.cookies());

        return response.parse();
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
                        log.error("Не удалось преобразовать полученный текст [{}] в кол-во объявлений. Ошибка: {}", pCount, e.getLocalizedMessage());
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
     * Сменить пользовательский браузер
     *
     * @param number рандомное число
     * @return рандомный браузер
     */
    private String switchUserAgent(int number) {
        String[] agents = {"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36"};
        return agents[number];
    }

}
