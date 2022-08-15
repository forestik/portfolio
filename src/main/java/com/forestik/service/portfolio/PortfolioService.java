package com.forestik.service.portfolio;

import com.forestik.dto.portfolio.Token;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public interface PortfolioService {

    void processData() throws IOException, NoSuchAlgorithmException, InvalidKeyException;

    String getDescription();

    HashMap<String, List<Token>> getProcessedRecords() throws NoSuchAlgorithmException, InvalidKeyException;
}
