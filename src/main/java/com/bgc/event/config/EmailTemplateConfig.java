package com.bgc.event.config;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.config
 * - File       : EmailTemplateConfig.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Dedicated Thymeleaf TemplateEngine for rendering email bodies.
 *
 *   Why a separate engine?
 *   The web engine is managed by Spring MVC autoconfiguration and serves
 *   HTTP responses.  Emails are rendered off-thread to a plain String —
 *   a separate engine avoids any interaction with the web rendering cycle.
 *
 *   Resolver: ClassLoaderTemplateResolver → templates/email/*
 *   MessageSource: same i18n/messages*.properties used by the web UI
 * </pre>
 */

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class EmailTemplateConfig {

    /**
     * Template engine that resolves files under classpath:templates/email/.
     * Uses the shared MessageSource so #{email.reset.*} keys work in both
     * EN and FR depending on the user's stored locale preference.
     */
    @Bean(name = "emailTemplateEngine")
    public TemplateEngine emailTemplateEngine(MessageSource messageSource) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);       // cache in production
        resolver.setOrder(1);
        resolver.setCheckExistence(true);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messageSource);
        return engine;
    }
}
