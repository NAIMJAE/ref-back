package kr.co.reference.config;

import kr.co.reference.webAnalytics.VisitorTracking.VisitorTrackingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload.path}")
    private String resourcePath;

    @Value("${front.url}")
    private String frontUrl;

    @Autowired
    private VisitorTrackingInterceptor visitorTrackingInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:"+resourcePath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://refcode.info", "https://refcode.shop")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Content-Disposition");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(visitorTrackingInterceptor)
                .addPathPatterns("/*")
                .excludePathPatterns("/tictactoe", "/health-check");

    }
}
