package com.distasilucas.cryptobalancetracker.configuration;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.EnumSet;

/**
 * TODO - This can be removed when logging issue is fixed.
 * https://github.com/spring-projects/spring-framework/issues/31588
 */

@Configuration
public class CacheHandlerMappingIntrospectorConfig {

    @Bean
    static FilterRegistrationBean<Filter> handlerMappingIntrospectorCacheFilter(HandlerMappingIntrospector hmi) {
        var cacheFilter = hmi.createCacheFilter();
        var registrationBean = new FilterRegistrationBean<>(cacheFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));

        return registrationBean;
    }
}
