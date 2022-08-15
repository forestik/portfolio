package com.forestik.handler;

import com.forestik.PortfolioRunner;
import com.forestik.enums.Command;
import com.forestik.messagesender.MessageSender;
import com.forestik.service.portfolio.trust.DefaultChainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.forestik.enums.Command.CONTRACT_ADDRESS;
import static com.forestik.enums.Command.RUN;


@Component
@AllArgsConstructor
@Slf4j
public class MessageHandler implements Handler {

    private final MessageSender messageSender;

    private final PortfolioRunner portfolioRunner;

    private final List<DefaultChainService> defaultChainServices;

    private static String chain = "";;
    private static Boolean ifAddContractAddress = false;

    @Override
    public void choose(Message message) {
        if (message.hasText()) {
            Command commandoEnum = RUN;
            if (ifAddContractAddress) {
                commandoEnum = CONTRACT_ADDRESS;
            }
            if (!ifAddContractAddress){
                commandoEnum = Command.getCommandoEnum(message.getText());
            }
            log.info("Command: {}", message.getText());
            switch (commandoEnum) {
                case RUN:
                    portfolioRun(message);
                    break;
                case ADD_TOKEN:
                    getChains(message, "add");
                    break;
                case TOKENS:
                    getChains(message, "get");
                    break;
                case SELECT_CHAIN:
                    selectChain(message);
                    break;
                case CONTRACT_ADDRESS:
                    addContractAddresses(message);
                    break;
            }
        }
    }

    private void portfolioRun(Message message) {
        portfolioRunner.run();
        SendMessage sendMessage = getSendMessage(message, "Portfolio cycle complete");
        messageSender.sendMessage(sendMessage);

    }

    private void getChains(Message message, String method) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Here is your keyboard")
                .build();
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        defaultChainServices.forEach(defaultChainService -> {
            KeyboardRow row = new KeyboardRow();
            row.add("/selectChain/" + method + "/" + defaultChainService.getChain());
            KeyboardButton keyboardButton = row.get(0);
            keyboardButton.setText("/selectChain/" + method + "/" + defaultChainService.getChain());
            keyboard.add(row);
        });
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        messageSender.sendMessage(sendMessage);
    }

    private void selectChain(Message message) {
        chain = message.getText().substring("/selectChain/".length() + 4);
        if (message.getText().startsWith("add", "/selectChain/".length())) {
            ifAddContractAddress = true;
            SendMessage sendMessage = getSendMessage(message, "Please paste contract address");
            messageSender.sendMessage(sendMessage);
        }
        if (message.getText().startsWith("get", "/selectChain/".length())) {
            SendMessage sendMessage = getSendMessage(message, "Please paste contract address");
            messageSender.sendMessage(sendMessage);
        }
    }

    private void addContractAddresses(Message message) {
        DefaultChainService chainService = defaultChainServices.stream().filter(defaultChainService -> defaultChainService.getChain().equals(chain)).findFirst().get();
        ifAddContractAddress = false;
        chainService.addToken(message.getText(), chain);
        SendMessage sendMessage = getSendMessage(message, "Contract address was added");
        messageSender.sendMessage(sendMessage);
    }

    private SendMessage getSendMessage(Message message, String text) {
        return SendMessage.builder()
                .text(text)
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
    }
}
