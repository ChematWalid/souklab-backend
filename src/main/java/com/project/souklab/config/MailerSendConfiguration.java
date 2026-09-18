package com.project.souklab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Configures bounded HTTP transport for the optional MailerSend integration. */
@Configuration
public class MailerSendConfiguration {

    /**
     * Creates the MailerSend client with externally configured connection and read timeouts.
     *
     * @param appProperties application configuration containing MailerSend timeouts
     * @return bounded MailerSend HTTP client
     */
    @Bean(name = "mailerSendRestTemplate")
    public RestTemplate mailerSendRestTemplate(AppProperties appProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(appProperties.getMailersend().getConnectionTimeout());
        requestFactory.setReadTimeout(appProperties.getMailersend().getReadTimeout());
        return new RestTemplate(requestFactory);
    }
}
