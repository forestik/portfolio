package com.forestik.dto.portfolio;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class KucoinCurrency {

    public Data data;

    @ToString
    @Getter
    public static class Data {

        public String sell;

        public String changePrice;

    }
}
