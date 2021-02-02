package com.ddkolesnik.avitotraiding.config;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.redcom.lib.integration.api.client.dadata.DaDataClient;
import ru.redcom.lib.integration.api.client.dadata.DaDataClientFactory;

/**
 * @author Alexandr Stegnin
 */

@Configuration
public class AppConfig {

    @Value("${dadata.api.key}")
    private String apiKey;

    @Value("${dadata.secret}")
    private String secret;

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

}
