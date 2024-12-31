package com.example.ebooking.config.bot;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class NotificationBotConfig {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.key}")
    private String token;
}
