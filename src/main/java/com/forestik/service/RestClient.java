package com.forestik.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RestClient {

    private final RestOperations restOperations;

    public static String getSignature(String data, String key, String hmacType, String encodeType) {
        byte[] hmacSha;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), hmacType);
            Mac mac = Mac.getInstance(hmacType);
            mac.init(secretKeySpec);
            hmacSha = mac.doFinal(data.getBytes((StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate " + hmacType, e);
        }
        if (encodeType.equals("hex")) {
            return Hex.encodeHexString(hmacSha);
        } else {
            return Base64.encodeBase64String(hmacSha);
        }
    }

    public <T> List<T> exchange(String url, HttpMethod httpMethod, HttpHeaders httpHeaders, String body, Object clazz) {
        RequestEntity<Object> requestEntity = new RequestEntity<>(body, httpHeaders, httpMethod, URI.create(url));
        ResponseEntity<List<T>> exchange = restOperations.exchange(requestEntity, (ParameterizedTypeReference<List<T>>) clazz);
        return exchange.getBody();
    }

    public <T> T getEntity(String url, HttpMethod httpMethod, HttpHeaders httpHeaders, String body, Class<T> clazz) {
        RequestEntity<Object> requestEntity = new RequestEntity<>(body, httpHeaders, httpMethod, URI.create(url));
        ResponseEntity<T> exchange = restOperations.exchange(requestEntity, clazz);
        return (T) exchange.getBody();
    }

    public HttpHeaders setHeader(String header, String headerValue) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(header, headerValue);
        return headers;
    }
}
