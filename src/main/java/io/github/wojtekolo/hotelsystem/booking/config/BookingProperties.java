package io.github.wojtekolo.hotelsystem.booking.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hotel.booking")
@Setter
@Getter
public class BookingProperties {
    @Min(1)
    private int maxDays = 365;
}
