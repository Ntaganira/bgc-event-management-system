package com.bgc.event;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event
 * - File       : BgcEventApplication.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Main entry point for BGC Event Management System
 * </pre>
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BgcEventApplication {
    public static void main(String[] args) {
        SpringApplication.run(BgcEventApplication.class, args);
    }
}
