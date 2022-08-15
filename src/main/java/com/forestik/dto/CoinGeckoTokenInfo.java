package com.forestik.dto;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class CoinGeckoTokenInfo {

    public String symbol;

    public MarketData market_data;

    public Image image;


    @ToString
    @Getter
    public static class Image {

        public String large;

    }


    @ToString
    @Getter
    public static class MarketData {

        public Double price_change_percentage_24h;

        public CurrentPrice current_price;
    }

    @ToString
    @Getter
    public static class CurrentPrice {

        public Double usd;
    }
}
