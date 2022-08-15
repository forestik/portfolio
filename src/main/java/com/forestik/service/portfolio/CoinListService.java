package com.forestik.service.portfolio;

import com.forestik.dto.portfolio.Token;
import com.forestik.service.CoinGeckoService;
import com.forestik.service.ExcelService;
import com.forestik.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;

//import org.apache.commons.codec.binary.Base64;

@Service
@Slf4j
public class CoinListService extends DefaultPortfolioService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String ENCODE_TYPE = "BASE64";

    public CoinListService(ExcelService excelService,
                           RestClient restClient,
                           CoinGeckoService coinGeckoService) {
        super(excelService, restClient, coinGeckoService);
    }

    public List<Token> getTokensInfo() {

        var timeStamp = System.currentTimeMillis()/1000;
        var path = "/v1/balances";
        var method = "GET";

        String payload = timeStamp + method + path;
        String s = new String(Base64.getDecoder().decode("NiuJv5i6MNfFPoFOqwUt3Gep2betp8+ftVWN5+lamz40bdhNxXw1iY2v2dld7v2c0gxQhkibXWCHm3a9X8vZNg==".getBytes(StandardCharsets.UTF_8)));
        String payload1 = Base64.getEncoder().encodeToString(payload.getBytes());
        String s1 = new String(org.apache.commons.codec.binary.Base64.decodeBase64("NiuJv5i6MNfFPoFOqwUt3Gep2betp8+ftVWN5+lamz40bdhNxXw1iY2v2dld7v2c0gxQhkibXWCHm3a9X8vZNg=="));

        String signaturre = hmacWithApacheCommons(HMAC_SHA256, payload, s1);
        String signature1 = RestClient.getSignature(payload, s1, HMAC_SHA256, ENCODE_TYPE);
        String signature = RestClient.getSignature(payload, s, HMAC_SHA256, ENCODE_TYPE);
//        signature = Base64.getEncoder().encodeToString(signature.getBytes());

        String s2 = Base64.getEncoder().encodeToString(signaturre.getBytes());


//        var httpBody = null;
        var message = timeStamp + method + path + "";
        var signatureeeee = new HmacUtils(HMAC_SHA_256, s.getBytes((StandardCharsets.UTF_8))).hmacHex(message);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/json");
        headers.set("CL-ACCESS-KEY", "fc7f1110-0534-4233-bb1b-49c13b81fc51");
        headers.set("CL-ACCESS-SIG", signature);
        headers.set("CL-ACCESS-TIMESTAMP", String.valueOf(timeStamp));
        Object coinList = restClient.getEntity("https://trade-api.coinlist.co/v1/balances",
                HttpMethod.GET,
                headers,
                null,
                Object.class);
        ArrayList<Token> walletTokenInfos = new ArrayList<>();

        return walletTokenInfos;
    }

    @Override
    public HashMap<String, List<Token>> getProcessedRecords() {
        return new HashMap<>();
    }

    public static String hmacWithApacheCommons(String algorithm, String data, String key) {
        String hmac = new HmacUtils(algorithm, key).hmacHex(data);
        return hmac;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
