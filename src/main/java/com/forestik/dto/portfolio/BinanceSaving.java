package com.forestik.dto.portfolio;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class BinanceSaving {

    public String totalAmountInBTC;

    public String totalAmountInUSDT;

    public String totalFixedAmountInBTC;

    public String totalFixedAmountInUSDT;

    public String totalFlexibleInBTC;

    public String totalFlexibleInUSDT;

    public List<SavingToken> positionAmountVos;


    @ToString
    @Getter
    public static class SavingToken {

        public String amount;

        public String amountInBTC;

        public String amountInUSDT;

        public String asset;
    }

}
