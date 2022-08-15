package com.forestik.service;

import com.forestik.dto.CoinGeckoToken;
import com.forestik.dto.CoinGeckoTokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.List;

@Service
@Slf4j
public class CoinGeckoService extends RestClient {

    public CoinGeckoService(RestOperations restOperations) {
        super(restOperations);
    }

    public CoinGeckoTokenInfo getTokenInfo(String chain, String contractAddress) {
        return getEntity("https://api.coingecko.com/api/v3/coins/" + chain +
                        "/contract/" + contractAddress,
                HttpMethod.GET, null, null, CoinGeckoTokenInfo.class);

    }

    public CoinGeckoTokenInfo getTokenChainInfo(String symbol) {
        return getEntity("https://api.coingecko.com/api/v3/coins/" + symbol,
                HttpMethod.GET, null, null, CoinGeckoTokenInfo.class);

    }

    public List<CoinGeckoToken> getTokens() {
        ParameterizedTypeReference<List<CoinGeckoToken>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
        };
        return exchange("https://api.coingecko.com/api/v3/coins/list?include_platform=false",
                HttpMethod.GET, null, null, parameterizedTypeReference);
    }
}
