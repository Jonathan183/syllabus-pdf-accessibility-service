package edu.gsw.syllabus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    /**
     * For production, LOCK THIS DOWN:
     * Replace allowedOrigins("*") with your actual site origin(s), e.g.:
     *   .allowedOrigins("https://your-syllabus-site.gsw.edu")
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
