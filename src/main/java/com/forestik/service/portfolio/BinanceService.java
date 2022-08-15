package com.forestik.service.portfolio;

import com.forestik.dto.CoinGeckoToken;
import com.forestik.dto.CoinGeckoTokenInfo;
import com.forestik.dto.portfolio.BinanceSaving;
import com.forestik.dto.portfolio.BinanceSpotToken;
import com.forestik.dto.portfolio.BinanceTokenInfo;
import com.forestik.dto.portfolio.Token;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BinanceService extends DefaultPortfolioService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String ENCODE_TYPE = "hex";

    private final String apiKey;
    private final String secretKey;

    public BinanceService(ExcelService excelService,
                          RestClient restClient,
                          CoinGeckoService coinGeckoService,
                          @Value("${binance.apiKey}") String apiKey,
                          @Value("${binance.secretKey}") String secretKey) {
        super(excelService, restClient, coinGeckoService);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public List<BinanceSpotToken> getSpotInfo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String query = "recvWindow=50000&timestamp=" + timestamp;
        String signature = RestClient.getSignature(query, secretKey, HMAC_SHA256, ENCODE_TYPE);
        ParameterizedTypeReference<List<BinanceSpotToken>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
        };
        List<BinanceSpotToken> binanceCoins = restClient.exchange("https://api.binance.com/sapi/v1/capital/config/getall?" + query + "&signature=" + signature,
                HttpMethod.GET,
                restClient.setHeader("X-MBX-APIKEY", apiKey),
                null,
                parameterizedTypeReference);
        return binanceCoins.stream().filter(binanceCoin -> Double.parseDouble(binanceCoin.getFree()) > 0).collect(Collectors.toList());
    }

    public BinanceSaving getSavingInfo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String query = "recvWindow=50000&timestamp=" + timestamp;
        String signature = RestClient.getSignature(query, secretKey, HMAC_SHA256, ENCODE_TYPE);
        return restClient.getEntity("https://api.binance.com/sapi/v1/lending/union/account?" + query + "&signature=" + signature,
                HttpMethod.GET,
                restClient.setHeader("X-MBX-APIKEY", apiKey),
                null,
                BinanceSaving.class);
    }

    public List<Token> enrichSpotInfo() {
        List<BinanceSpotToken> spotInfo = getSpotInfo();
        ArrayList<Token> walletTokenInfos = new ArrayList<>();
        List<CoinGeckoToken> tokens = coinGeckoService.getTokens();
        spotInfo.forEach(s -> {
            String query = "symbol=" + s.getCoin() + "BUSD";
            BinanceTokenInfo binanceTokenInfo = restClient.getEntity("https://api.binance.com/api/v3/ticker/24hr?" + query,
                    HttpMethod.GET,
                    restClient.setHeader("X-MBX-APIKEY", apiKey),
                    null,
                    BinanceTokenInfo.class);
            Double priceChangePercentage24h = binanceTokenInfo.priceChangePercent != null
                    ? Double.parseDouble(binanceTokenInfo.priceChangePercent)
                    : getCoinGeckoPriceChangeInfo(tokens, s.getCoin().toLowerCase());
            double currentPrice = binanceTokenInfo.lastPrice != null
                    ? Double.parseDouble(binanceTokenInfo.lastPrice)
                    : getCoinGeckoCurrentPriceInfo(tokens, s.getCoin().toLowerCase());
            ;
            Token build = Token.builder()
                    .symbol(s.getCoin())
                    .priceChangePercentage24h(priceChangePercentage24h)
                    .currentPrice(currentPrice)
                    .totalAmountInUSDT(currentPrice != 0.0
                            ? Double.parseDouble(s.getFree()) * currentPrice
                            : 0.0)
                    .amount(s.getFree())
                    .build();
            walletTokenInfos.add(build);
        });
        List<Token> collect = walletTokenInfos.stream().sorted((t1, t2) -> t2.totalAmountInUSDT.compareTo(t1.totalAmountInUSDT)).collect(Collectors.toList());
        return collect;
    }

    public List<Token> enrichSavingInfo() {
        BinanceSaving savingInfo = getSavingInfo();
        List<BinanceSaving.SavingToken> positionAmountVos = savingInfo.getPositionAmountVos();
        ArrayList<Token> walletTokenInfos = new ArrayList<>();
        List<CoinGeckoToken> tokens = coinGeckoService.getTokens();
        positionAmountVos.forEach(s -> {
            String query = "symbol=" + s.getAsset() + "BUSD";
            BinanceTokenInfo binanceTokenInfo = restClient.getEntity("https://api.binance.com/api/v3/ticker/24hr?" + query,
                    HttpMethod.GET,
                    restClient.setHeader("X-MBX-APIKEY", apiKey),
                    null,
                    BinanceTokenInfo.class);
            double priceChangePercentage24h = binanceTokenInfo.priceChangePercent != null
                    ? Double.parseDouble(binanceTokenInfo.priceChangePercent)
                    : getCoinGeckoPriceChangeInfo(tokens, s.getAsset().toLowerCase());
            Double currentPrice = binanceTokenInfo.lastPrice != null
                    ? Double.parseDouble(binanceTokenInfo.lastPrice)
                    : getCoinGeckoCurrentPriceInfo(tokens, s.getAsset().toLowerCase());
            ;
            Token build = Token.builder()
                    .symbol(s.getAsset())
                    .priceChangePercentage24h(priceChangePercentage24h)
                    .currentPrice(currentPrice)
                    .totalAmountInUSDT(Double.parseDouble(s.getAmountInUSDT()))
                    .amount(s.getAmount())
                    .build();
            walletTokenInfos.add(build);
        });
        return walletTokenInfos;
    }

    public double getCoinGeckoPriceChangeInfo(List<CoinGeckoToken> tokens, String symbol) {
        Optional<CoinGeckoToken> first = tokens.stream()
                .filter(token -> token.getSymbol().equals(symbol)).findFirst();
        CoinGeckoTokenInfo tokenInfo;
        if (first.isPresent()) {
            tokenInfo = coinGeckoService.getTokenChainInfo(first.get().getId());
            return tokenInfo.market_data.price_change_percentage_24h;
        }
        return 0.0;
    }

    public double getCoinGeckoCurrentPriceInfo(List<CoinGeckoToken> tokens, String symbol) {
        Optional<CoinGeckoToken> first = tokens.stream()
                .filter(token -> token.getSymbol().equals(symbol)).findFirst();
        CoinGeckoTokenInfo tokenInfo;
        if (first.isPresent()) {
            tokenInfo = coinGeckoService.getTokenChainInfo(first.get().getId());
            return tokenInfo.market_data.current_price.usd;
        }
        return 0.0;
    }

    @Override
    public HashMap<String, List<Token>> getProcessedRecords() {
        HashMap<String, List<Token>> hashMap = new HashMap<>();
        hashMap.put("Spot_Binance", enrichSpotInfo());
        hashMap.put("Saving_Binance", enrichSavingInfo());
        return hashMap;
    }

    @Override
    public String getDescription() {
        return String.format("%s", this.getClass().getSimpleName());
    }
}
