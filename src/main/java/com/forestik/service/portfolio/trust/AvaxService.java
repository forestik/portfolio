package com.forestik.service.portfolio.trust;

import com.forestik.dto.portfolio.Token;
import com.forestik.repo.ContractAddressesRepo;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class AvaxService extends DefaultChainService {

    private static final String BASE_URL = "api.snowtrace.io/api";
    private static final String CHAIN = "avalanche";
    private static final String CHAIN_TOKEN = "avalanche-2";

    private final String apiKey;

    public AvaxService(ContractAddressesRepo contractAddressesRepo,
                      RestClient restClient,
                      ExcelService excelService,
                      CoinGeckoService coinGeckoService,
                      @Value("${wallet.apiKey.avax}") String apiKey) {
        super(excelService, restClient, coinGeckoService, contractAddressesRepo);
        this.apiKey = apiKey;
    }

    public List<Token> getTokensInfo(){
        return getTokensInfo(BASE_URL, "0x93B63f0c89eaf46424b5b479b6029ba4A4C0C3B7", apiKey, CHAIN, CHAIN_TOKEN);
    }

    @Override
    public HashMap<String, List<Token>> getProcessedRecords() {
        HashMap<String, List<Token>> hashMap = new HashMap<>();
        hashMap.put("AVAX", getTokensInfo());
        return hashMap;
    }

    public String getChain(){
        return CHAIN;
    }
}
