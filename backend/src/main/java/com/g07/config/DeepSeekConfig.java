package com.g07.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DeepSeekConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置超时时间，避免 AI 响应慢导致报错
        factory.setConnectTimeout(60000); // 10秒连接超时
        factory.setReadTimeout(600000);    // 60秒读取超时
        return new RestTemplate(factory);
    }
}