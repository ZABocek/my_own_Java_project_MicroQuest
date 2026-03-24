package com.example.microquest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String storageLocation;

    public WebMvcConfig(@Value("${storage.location:uploads}") String storageLocation) {
        this.storageLocation = storageLocation;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve GIF uploads publicly
        registry.addResourceHandler("/uploads/gifs/**")
                .addResourceLocations("file:" + storageLocation + "/gifs/");
    }
}
