package com.forestik.dto.portfolio;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class KucoinToken {

    public List<Token> data;

    @ToString
    @Getter
    public static class Token {
        public String currency;

        public String balance;
    }
}
