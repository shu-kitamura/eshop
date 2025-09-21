package com.skishop.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * MongoDB configuration (using Java 21 modern syntax)
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "skishop_inventory";
    }

    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        // Utilize Java 21 method references and functional programming
        return new MongoCustomConversions(List.of(
            localDateTimeToZonedDateTimeConverter(),
            zonedDateTimeToLocalDateTimeConverter()
        ));
    }

    /**
     * LocalDateTime to ZonedDateTime converter (functional)
     */
    private Converter<LocalDateTime, ZonedDateTime> localDateTimeToZonedDateTimeConverter() {
        return source -> source.atZone(ZoneId.systemDefault());
    }

    /**
     * ZonedDateTime to LocalDateTime converter (functional)
     */
    private Converter<ZonedDateTime, LocalDateTime> zonedDateTimeToLocalDateTimeConverter() {
        return ZonedDateTime::toLocalDateTime;
    }
}
