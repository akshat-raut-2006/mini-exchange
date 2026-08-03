package com.matchingengine.api.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS is registered as an actual Filter bean here, not via
 * WebMvcConfigurer#addCorsMappings. This matters specifically because of
 * JwtAuthFilter: when that filter rejects a request (missing/invalid
 * token) it calls response.sendError() directly and never reaches
 * DispatcherServlet - and WebMvcConfigurer-based CORS handling only runs
 * inside DispatcherServlet. The result was every 401 from JwtAuthFilter
 * going out with no CORS headers at all, which browsers report as an
 * opaque CORS failure ("Failed to fetch") instead of a readable 401.
 *
 * Registering CORS as its own Filter, explicitly ordered to run before
 * JwtAuthFilter (see @Order on JwtAuthFilter), guarantees CORS headers
 * are attached to every response - including ones JwtAuthFilter rejects.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}