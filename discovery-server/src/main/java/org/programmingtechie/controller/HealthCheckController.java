package org.programmingtechie.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@RestController
public class HealthCheckController {
    @Value("${info.app.version}")
    private String appVersion;

    @Value("${info.app.release-note}")
    private String releaseNote;

    @Value("classpath:git.properties")
    private Resource gitPropertiesResource;

    @GetMapping("/health-check")
    public Map<String, Object> health() throws IOException {
        Properties gitProperties = new Properties();
        gitProperties.load(Files.newBufferedReader(Paths.get(gitPropertiesResource.getURI())));

        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("appVersion", appVersion);
        healthStatus.put("releaseNote", releaseNote);

        for (String key : gitProperties.stringPropertyNames()) {
            healthStatus.put(key, gitProperties.getProperty(key));
        }

        return healthStatus;
    }
}
