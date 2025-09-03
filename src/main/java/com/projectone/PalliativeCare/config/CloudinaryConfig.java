package com.projectone.PalliativeCare.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalStateException("cloudinary.url property not set");
        }
        return new Cloudinary(cloudinaryUrl);
    }

    @Bean
    public Uploader uploader(Cloudinary cloudinary) {
        return cloudinary.uploader();
    }
}
