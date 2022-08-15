package com.forestik.service;

import com.forestik.handler.Handler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final String botUserName;

    private final String token;

    private final Handler handler;

//    public TelegramBot(@Value("${telegram.bot.userName}") String botUserName,
//                       @Value("${telegram.bot.token}") String token,
//                       Handler handler) {
//        this.botUserName = botUserName;
//        this.token = token;
//        this.handler = handler;
//    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().getText().contains("/")) {
//            handler.choose(update.getMessage());
//        }
        handler.choose(update.getMessage());
    }

    @Scheduled(cron = "0 50 8 * * 3")
    public void testScheduled() throws TelegramApiException {
        log.info("Scheduled meeting");
        SendMessage build = SendMessage.builder()
                .text("<b>Hey sCUMers ⊙_⊙\r\n" +
                        "We have a meeting scheduled for 9:00<b>")
                .parseMode("HTML")
                .chatId("-1001586818258")
                .build();
        this.execute(build);
    }
}
