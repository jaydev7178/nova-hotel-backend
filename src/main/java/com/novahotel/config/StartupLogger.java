package com.novahotel.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StartupLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        String url = env.getProperty("spring.datasource.url", env.getProperty("spring.datasource.jdbc-url", "(not set)"));
        String user = env.getProperty("spring.datasource.username", "(not set)");
        String profiles = Arrays.toString(env.getActiveProfiles());
        System.out.println("[StartupLogger] Active profiles: " + profiles);
        System.out.println("[StartupLogger] Resolved spring.datasource.url: " + url);
        System.out.println("[StartupLogger] Resolved spring.datasource.username: " + user);
    }
}
