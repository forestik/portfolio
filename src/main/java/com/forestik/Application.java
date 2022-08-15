package com.forestik;

import com.forestik.dto.portfolio.Token;
import com.forestik.service.portfolio.CoinListService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SpringBootApplication()
@EnableScheduling
public class Application {

    private static CoinListService coinListService;


    public Application(CoinListService coinListService){
        Application.coinListService = coinListService;
    }

    public static void main(String[] args) throws TelegramApiException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        SpringApplication.run(Application.class, args);

          List<Token> tradingTokensInfo = coinListService.getTokensInfo();

          System.out.println(tradingTokensInfo.toString());

    }
}
