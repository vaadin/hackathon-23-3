package com.vaadin.example.hackathon233;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.vaadin.collaborationengine.CollaborationEngineConfiguration;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "hackathon-23-3")
@PWA(name = "Hackathon-23-3", shortName = "Hackathon-23-3", offlineResources = {})
@Push
@EntityScan(basePackageClasses = { Application.class })
@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Uncomment this annotation for production
    // @Bean
    public CollaborationEngineConfiguration ceConfigBean() {
        CollaborationEngineConfiguration configuration = new CollaborationEngineConfiguration(e -> {
        });
        String folder = System.getProperty("user.home") + "/ce";
        Path path = Paths.get(folder);
        if (!Files.isDirectory(path)) {
            throw new RuntimeException(
                    "\n\n\n>>>>>>>\n\nCreate the folder '" + folder
                            + "' and put a ce-license.json file\n\n>>>>>>>\n\n");
        }
        configuration.setDataDir(folder);
        return configuration;
    }

}
