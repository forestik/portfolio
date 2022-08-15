package com.forestik.messagesender;

import com.forestik.service.TelegramBot;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MessageSenderImpl implements MessageSender {

    @Lazy
    private final TelegramBot telegramBot;

    public MessageSenderImpl(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }


    @Override
    public void sendMessage(SendMessage sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
