package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.repository.Grabber;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    private String switchUserAgent(int number) {
        String[] agents = {"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36"};
        return agents[number];
    }

}
