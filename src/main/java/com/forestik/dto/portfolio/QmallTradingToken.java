package com.forestik.dto.portfolio;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;

@ToString
@Getter
public class QmallTradingToken {

    public Boolean success;

    public String message;

    public HashMap<String,Amount> result;

    @ToString
    @Getter
    public static class Amount {

        public String available;

        public String freeze;
    }
}
