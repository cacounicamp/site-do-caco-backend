package com.caco.sitedocaco.infrastructure.storage.imgBB;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ImgBBConfig {

    @Value("${imgbb.api.key}")
    private String apiKey;

    @Value("${imgbb.api.url:https://api.imgbb.com/1/upload}")
    private String apiUrl;

}