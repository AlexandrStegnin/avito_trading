package com.ddkolesnik.avitotraiding.config;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.scraperapi.ScraperApiClient;
import kong.unirest.Unirest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.redcom.lib.integration.api.client.dadata.DaDataClient;
import ru.redcom.lib.integration.api.client.dadata.DaDataClientFactory;

/**
 * @author Alexandr Stegnin
 */

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppConfig {

    @Value("${dadata.api.key}")
    String apiKey;

    @Value("${dadata.secret}")
    String secret;

    @Value("${scraper.api.key}")
    String scraperApiKey;

    @Bean
    public WebClient webClient() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        configureOptions(webClient);
        return webClient;
    }

    private void configureOptions(WebClient client) {
        WebClientOptions options = client.getOptions();
        options.setTimeout(0);
        options.setCssEnabled(true);
        options.setJavaScriptEnabled(false);
        options.setThrowExceptionOnScriptError(false);
        options.setPrintContentOnFailingStatusCode(false);
        options.setThrowExceptionOnFailingStatusCode(false);
    }

    @Bean
    public DaDataClient daDataClient() {
        return DaDataClientFactory.getInstance(apiKey, secret);
    }

    @Bean
    public ScraperApiClient scraperApiClient() {
        Unirest.config().socketTimeout(0).connectTimeout(0);
        return new ScraperApiClient(scraperApiKey);
    }

}
