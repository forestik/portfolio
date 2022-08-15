package com.forestik.dto.portfolio;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class Token {

    public String symbol;

    public Double currentPrice;

    public Double priceChangePercentage24h;

    public Double totalAmountInUSDT;

    public String image;

    public String amount;
}
