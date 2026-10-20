package com.megamart.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${DATABASE_JDBC_URL:}") String jdbcUrl,
            @Value("${PGUSER:${DB_USERNAME:}}") String fallbackUsername,
            @Value("${PGPASSWORD:${DB_PASSWORD:}}") String fallbackPassword) {
        ConnectionDetails details = connectionDetails(databaseUrl, jdbcUrl, fallbackUsername, fallbackPassword);
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("org.postgresql.Driver")
                .url(details.url())
                .username(details.username())
                .password(details.password())
                .build();
    }

    private ConnectionDetails connectionDetails(String databaseUrl, String jdbcUrl,
                                                String fallbackUsername, String fallbackPassword) {
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            return new ConnectionDetails(jdbcUrl, fallbackUsername, fallbackPassword);
        }
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("Set Railway's DATABASE_URL or DATABASE_JDBC_URL before starting the store.");
        }
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            return new ConnectionDetails(databaseUrl, fallbackUsername, fallbackPassword);
        }

        URI uri = URI.create(databaseUrl);
        if (!"postgresql".equals(uri.getScheme()) && !"postgres".equals(uri.getScheme())) {
            throw new IllegalStateException("DATABASE_URL must be a PostgreSQL connection URL.");
        }
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalStateException("DATABASE_URL does not contain a valid database host.");
        }
        String formattedHost = host.contains(":") ? "[" + host + "]" : host;
        String jdbc = "jdbc:postgresql://" + formattedHost
                + (uri.getPort() >= 0 ? ":" + uri.getPort() : "")
                + (uri.getRawPath() == null ? "" : uri.getRawPath())
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

        String username = fallbackUsername;
        String password = fallbackPassword;
        String rawUserInfo = uri.getRawUserInfo();
        if (rawUserInfo != null) {
            int separator = rawUserInfo.indexOf(':');
            username = decode(separator < 0 ? rawUserInfo : rawUserInfo.substring(0, separator));
            if (separator >= 0) {
                password = decode(rawUserInfo.substring(separator + 1));
            }
        }
        return new ConnectionDetails(jdbc, username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private record ConnectionDetails(String url, String username, String password) { }
}
