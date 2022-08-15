package com.forestik.service.portfolio.trust;

import com.forestik.dto.CoinGeckoTokenInfo;
import com.forestik.dto.portfolio.Token;
import com.forestik.dto.portfolio.trust.WalletToken;
import com.forestik.entity.ContractAddress;
import com.forestik.repo.ContractAddressesRepo;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import com.forestik.service.portfolio.DefaultPortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public abstract class DefaultChainService extends DefaultPortfolioService {

    private final ContractAddressesRepo contractAddressesRepo;

    public DefaultChainService(ExcelService excelService,
                               RestClient restClient,
                               CoinGeckoService coinGeckoService,
                               @Autowired ContractAddressesRepo contractAddressesRepo) {
        super(excelService, restClient, coinGeckoService);
        this.contractAddressesRepo = contractAddressesRepo;
    }

    protected WalletToken getWalletTokenInfo(String url, String contractaddress, String address, String apikey) {
        WalletToken walletToken = restClient.getEntity("https://" + url + "?module=account&action=tokenbalance" +
                        "&contractaddress=" + contractaddress +
                        "&address=" + address +
                        "&tag=latest" +
                        "&apikey=" + apikey,
                HttpMethod.GET, null, null, WalletToken.class);
        if (walletToken.status.equals("1") && walletToken.result.length() > 18) {
            int position = walletToken.result.length() - 18;
            walletToken.result = walletToken.result.substring(0, position) + "." + walletToken.result.substring(position);
        }
        return walletToken;
    }

    protected WalletToken getWalletTokenChainInfo(String url, String address, String apikey) {
        WalletToken walletToken = restClient.getEntity("https://" + url + "?module=account&action=balance" +
                        "&address=" + address +
                        "&apikey=" + apikey,
                HttpMethod.GET, null, null, WalletToken.class);

        if (walletToken.status.equals("1")) {
            if (walletToken.result.length() > 18) {
                int position = walletToken.result.length() - 18;
                walletToken.result = walletToken.result.substring(0, position) + "." + walletToken.result.substring(position);
            }
            if (walletToken.result.length() <= 18) {
                int i = 18 - walletToken.result.length();
                walletToken.result = "0." + "0".repeat(i) + walletToken.result;
            }
        }
        return walletToken;
    }

    protected Token getToken(WalletToken tokenInfo, CoinGeckoTokenInfo coinGeckoTokenInfo) {
        return Token.builder()
                .symbol(coinGeckoTokenInfo.symbol.toUpperCase())
                .currentPrice(coinGeckoTokenInfo.market_data.current_price.usd)
                .priceChangePercentage24h(coinGeckoTokenInfo.market_data.price_change_percentage_24h)
                .totalAmountInUSDT(Double.parseDouble(tokenInfo.result) * coinGeckoTokenInfo.market_data.current_price.usd)
                .image(coinGeckoTokenInfo.image.large)
                .amount(tokenInfo.result)
                .build();
    }

    protected List<Token> getTokensInfo(String url, String address, String apiKey, String chain, String chainToken){
        List<String> contractAddresses = new ArrayList<>();
        List<ContractAddress> allByChain = contractAddressesRepo.findAllByChain(getChain());
        allByChain.forEach(contractAddress -> contractAddresses.add(contractAddress.getContractAddress()));
        List<Token> tokens = new ArrayList<>();
        var tokenChainInfo = getWalletTokenChainInfo(url, address, apiKey);
        var coinGeckoChainTokenInfo = coinGeckoService.getTokenChainInfo(chainToken);
        Token walletTokenChainInfo = getToken(tokenChainInfo, coinGeckoChainTokenInfo);
        tokens.add(walletTokenChainInfo);
        contractAddresses.forEach(token -> {
            WalletToken tokenInfo = getWalletTokenInfo(url, token, address, apiKey);
            CoinGeckoTokenInfo coinGeckoTokenInfo = coinGeckoService.getTokenInfo(chain, token);
            tokens.add(getToken(tokenInfo, coinGeckoTokenInfo));
        });
        return tokens;
    }

    public void addToken(String contractAddress, String chain) {
        ContractAddress address = ContractAddress.builder()
                .chain(chain)
                .contractAddress(contractAddress)
                .build();
        contractAddressesRepo.save(address);
    }

    @Override
    public String getDescription() {
        return String.format("%s", this.getClass().getSimpleName());
    }

    public abstract String getChain();
}
