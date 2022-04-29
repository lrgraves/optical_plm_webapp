package com.eleoptics.spark.config;

import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.views.project.DownloadServlet;
import com.google.common.eventbus.EventBus;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class ApiConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public OpticsApi opticsApi() {
        return new OpticsApi();
    }

    @Bean
    public ServletRegistrationBean downloadServletRegistration(OpticsApi opticsApi){
        return new ServletRegistrationBean(new DownloadServlet(opticsApi),"/download");
    }

    @Bean
    public EventBus eventBus(){
        return new EventBus();
    }

}
