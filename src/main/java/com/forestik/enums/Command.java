package com.forestik.enums;

import java.util.Arrays;

public enum Command {
    RUN("/run"),
    ADD_TOKEN("/addToken"),
    SELECT_CHAIN("/selectChain"),
    CONTRACT_ADDRESS("/contractAddress"),
    TOKENS("/tokens"),
    CALENDAR("/calendar"),
    EXCEL("/excel");

    public String command;

    Command(String s) {
        this.command = s;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommandoEnum(String value) {
        return Arrays.stream(Command.values()).filter(e -> value.contains(e.getCommand())).findFirst().orElseThrow();
    }

}
