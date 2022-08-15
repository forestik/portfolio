package com.forestik.service.portfolio;

import com.forestik.dto.CoinGeckoToken;
import com.forestik.dto.portfolio.Token;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

@Service
public abstract class DefaultPortfolioService implements PortfolioService{

    private final ExcelService excelService;
    protected final CoinGeckoService coinGeckoService;
    protected final RestClient restClient;

    public DefaultPortfolioService(ExcelService excelService,
                                   RestClient restClient,
                                   CoinGeckoService coinGeckoService) {
        this.excelService = excelService;
        this.restClient = restClient;
        this.coinGeckoService = coinGeckoService;
    }

    @Override
    public void processData() throws NoSuchAlgorithmException, InvalidKeyException {
        final var startTime = System.currentTimeMillis();
        final var processedRecords = getProcessedRecords();
        doPublishRecords(processedRecords);
    }

    public void doPublishRecords(HashMap<String, List<Token>> records) {
        records.forEach((k,v) -> {
            try {
                excelService.write(k, v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Token> enrichTokens(){
        List<CoinGeckoToken> tokens = coinGeckoService.getTokens();
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
