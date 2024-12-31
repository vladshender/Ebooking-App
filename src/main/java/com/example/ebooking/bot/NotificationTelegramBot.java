package com.example.ebooking.bot;

import com.example.ebooking.config.bot.NotificationBotConfig;
import com.example.ebooking.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
@Component
public class NotificationTelegramBot extends TelegramLongPollingBot {
    private final NotificationBotConfig botConfig;

    private Long adminChatId;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            if (messageText.equalsIgnoreCase("/start")) {
                handleStartCommand(chatId, userName);
            }
        }
    }

    private void handleStartCommand(Long chatId, String userName) {
        adminChatId = chatId;
        sendMessage(chatId, "Hi, " + userName
                + "! You have successfully started using the chat");
    }

    public void sendNotification(String message) {
        if (adminChatId != null) {
            sendMessage(adminChatId, message);
        } else {
            System.out.println("The bot was not initialized.");
        }
    }

    private void sendMessage(Long chatId, String messageText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(messageText);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new NotificationException("Сould not send a message "
                    + "to the user from the chat id: " + chatId);
        }
    }
}
