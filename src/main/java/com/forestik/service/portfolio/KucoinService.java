package com.forestik.service.portfolio;

import com.forestik.dto.portfolio.KucoinCurrency;
import com.forestik.dto.portfolio.KucoinToken;
import com.forestik.dto.portfolio.Token;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class KucoinService extends DefaultPortfolioService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String API_KEY = "62a71830ec648000018744d7";
    private static final String API_SECRET = "32f4a3cc-27d5-4974-9d60-2b8d700c24e2";
    private static final String API_PASSPHRASE = "portfolio";

    public KucoinService(ExcelService excelService,
                         RestClient restClient,
                         CoinGeckoService coinGeckoService) {
        super(excelService, restClient, coinGeckoService);
    }

    public List<Token> getTokensInfo() {

        AtomicLong timeStamp = new AtomicLong(System.currentTimeMillis());
        AtomicReference<String> path = new AtomicReference<>("/api/v1/accounts");
        var method = "GET";

        AtomicReference<String> payload = new AtomicReference<>(timeStamp + method + path);

        AtomicReference<String> signature = new AtomicReference<>(RestClient.getSignature(payload.get(), API_SECRET, HMAC_SHA256, "BASE64"));
        AtomicReference<String> passphrase = new AtomicReference<>(RestClient.getSignature(API_PASSPHRASE, API_SECRET, HMAC_SHA256, "BASE64"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/json");
        headers.set("KC-API-KEY", API_KEY);
        headers.set("KC-API-SIGN", signature.get());
        headers.set("KC-API-PASSPHRASE", passphrase.get());
        headers.set("KC-API-KEY-VERSION", "2");
        headers.set("KC-API-TIMESTAMP", String.valueOf(timeStamp.get()));
        KucoinToken exchange = restClient.getEntity("https://openapi-v2.kucoin.com/api/v1/accounts",
                HttpMethod.GET,
                headers,
                null,
                KucoinToken.class);

        ArrayList<Token> walletTokenInfos = new ArrayList<>();

        exchange.getData().stream().filter(c -> !c.getBalance().equals("0")).forEach(c -> {
            timeStamp.set(System.currentTimeMillis());
            path.set("/api/v1/market/stats?symbol=" + c.getCurrency() + "-USDT");

            payload.set(timeStamp + method + path);

            signature.set(RestClient.getSignature(payload.get(), API_SECRET, HMAC_SHA256, "BASE64"));
            passphrase.set(RestClient.getSignature(API_PASSPHRASE, API_SECRET, HMAC_SHA256, "BASE64"));
            headers.set("Content-type", "application/json");
            headers.set("KC-API-KEY", API_KEY);
            headers.set("KC-API-SIGN", signature.get());
            headers.set("KC-API-PASSPHRASE", passphrase.get());
            headers.set("KC-API-KEY-VERSION", "2");
            headers.set("KC-API-TIMESTAMP", String.valueOf(timeStamp.get()));
            KucoinCurrency entity = restClient.getEntity("https://openapi-v2.kucoin.com" + path,
                    HttpMethod.GET,
                    headers,
                    null,
                    KucoinCurrency.class);
            Double priceChangePercentage24h = entity.getData().getChangePrice() != null
                    ? Double.parseDouble(entity.getData().getChangePrice())
                    : 0.0;
            double currentPrice = entity.getData().getSell() != null
                    ? Double.parseDouble(entity.getData().getSell())
                    : 0.0;
            Token walletTokenChainInfo = Token.builder()
                    .symbol(c.getCurrency())
                    .currentPrice(currentPrice)
                    .priceChangePercentage24h(priceChangePercentage24h)
                    .totalAmountInUSDT(Double.parseDouble(c.getBalance()) * currentPrice)
                    .image(null)
                    .amount(c.getBalance())
                    .build();
            walletTokenInfos.add(walletTokenChainInfo);
        });

        return walletTokenInfos;
    }


    @Override
    public HashMap<String, List<Token>> getProcessedRecords() {
        HashMap<String, List<Token>> hashMap = new HashMap<>();
        hashMap.put("Kucoin", getTokensInfo());
        return hashMap;
    }

    @Override
    public String getDescription() {
        return String.format("%s", this.getClass().getSimpleName());
    }
}
