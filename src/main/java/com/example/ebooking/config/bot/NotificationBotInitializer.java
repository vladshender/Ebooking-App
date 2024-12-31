package com.example.ebooking.config.bot;

import com.example.ebooking.bot.NotificationTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class NotificationBotInitializer {
    private final NotificationTelegramBot notificationTelegramBot;

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(notificationTelegramBot);
            return telegramBotsApi;
        } catch (TelegramApiException e) {
            throw new RuntimeException("Error initializing TelegramBotsApi", e);
        }
    }
}
