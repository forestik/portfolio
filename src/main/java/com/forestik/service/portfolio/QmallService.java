package com.forestik.service.portfolio;

import com.forestik.dto.CoinGeckoToken;
import com.forestik.dto.CoinGeckoTokenInfo;
import com.forestik.dto.portfolio.QmallTradingToken;
import com.forestik.dto.portfolio.Token;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Slf4j
public class QmallService extends DefaultPortfolioService {

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String ENCODE_TYPE = "hex";
    private static final String API_PATH = "/api/v1/account/balances";
    private static final String API_KEY = "87bc5d9cf9fd2037d345c294eb4ae17e";
    private static final String API_SECRET = "588dd4df7e977372414c180498d19590";

    public QmallService(ExcelService excelService,
                        RestClient restClient,
                        CoinGeckoService coinGeckoService) {
        super(excelService, restClient, coinGeckoService);
    }


    public List<Token> getTradingTokensInfo() throws NoSuchAlgorithmException, InvalidKeyException {

        String dataJson =
                String.format("{\"request\":\"%1$s\",\"nonce\":\"%2$s\"}",
                        API_PATH,
                        System.currentTimeMillis());

        String payload = Base64.getEncoder().encodeToString(dataJson.getBytes());
        String signature = RestClient.getSignature(payload, API_SECRET, HMAC_SHA512, ENCODE_TYPE);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/json");
        headers.set("X-TXC-APIKEY", API_KEY);
        headers.set("X-TXC-PAYLOAD", payload);
        headers.set("X-TXC-SIGNATURE", signature);
        QmallTradingToken binanceCoins = restClient.getEntity("https://api.qmall.io/api/v1/account/balances",
                HttpMethod.POST,
                headers,
                dataJson,
                QmallTradingToken.class);
        ArrayList<Token> walletTokenInfos = new ArrayList<>();
        binanceCoins.getResult().forEach((key, value) -> {
            double totalAmount = Double.parseDouble(value.getAvailable())
                    + Double.parseDouble(value.getFreeze());
            if (totalAmount != 0) {
                List<CoinGeckoToken> tokens = coinGeckoService.getTokens();
                Optional<CoinGeckoToken> first = tokens.stream().filter(token -> token.getSymbol().equals(key.toLowerCase())).findFirst();
                CoinGeckoTokenInfo tokenInfo = new CoinGeckoTokenInfo();
                if(first.isPresent()){
                    tokenInfo = coinGeckoService.getTokenChainInfo(first.get().getId());
                }
                Token build = Token.builder()
                        .symbol(key)
                        .currentPrice(first.isPresent()
                                ? tokenInfo.market_data.current_price.usd
                                : 0.0)
                        .priceChangePercentage24h(first.isPresent()
                                ? tokenInfo.market_data.price_change_percentage_24h
                                : 0.0)
                        .totalAmountInUSDT(first.isPresent()
                                ? totalAmount * tokenInfo.market_data.current_price.usd
                                : 0.0)
                        .amount(Double.toString(totalAmount))
                        .build();
                walletTokenInfos.add(build);
            }
        });
        return walletTokenInfos;
    }


    @Override
    public HashMap<String, List<Token>> getProcessedRecords() throws NoSuchAlgorithmException, InvalidKeyException {
        HashMap<String, List<Token>> hashMap = new HashMap<>();
        hashMap.put("Qmall", getTradingTokensInfo());
        return hashMap;
    }

    @Override
    public String getDescription() {
        return String.format("%s", this.getClass().getSimpleName());
    }
}
