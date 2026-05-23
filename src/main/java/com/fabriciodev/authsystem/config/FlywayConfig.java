package com.fabriciodev.authsystem.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    /**
     * Bean customizado para garantir que Flyway execute durante o startup
     * Este bean força a execução das migrations antes que a aplicação inicie
     */
    @Bean
    public Flyway flyway(DataSource dataSource) {
        logger.info("Creating Flyway bean");
        logger.info("Flyway enabled: {}", flywayEnabled);
        logger.info("Migrations locations: {}", locations);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(true)
                .load();

        if (flywayEnabled) {
            logger.info("Running Flyway migrations...");
            flyway.migrate();
            logger.info("Flyway migrations completed successfully!");
        }

        return flyway;
    }
}


