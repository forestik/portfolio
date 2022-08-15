package com.forestik.config;

import com.forestik.handler.Handler;
import com.forestik.service.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBot telegramBot(@Value("${telegram.bot.userName}") String botUserName,
                                   @Value("${telegram.bot.token}") String token,
                                   Handler handler){
        TelegramBot telegramBot = new TelegramBot(botUserName, token, handler);

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return telegramBot;
    }

}
