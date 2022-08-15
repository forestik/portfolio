package com.forestik.service;

//import com.crypto.dto.TokensLeftMessageDto;
//import com.crypto.service.DefaultService;

import com.forestik.dto.portfolio.Token;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExcelService {

    private final Sheets sheets;

    private final String range;

    private final String spreadsheetId;

    public ExcelService(Sheets sheets,
                        @Value("${sheets.range}") String range,
                        @Value("${sheets.spreadsheetId}") String spreadsheetId
    ) {
        this.sheets = sheets;
        this.range = range;
        this.spreadsheetId = spreadsheetId;
    }

    public void write(String sheet, List<Token> walletInfo) throws IOException {
        List<List<Object>> tokensList = new ArrayList<>();
        tokensList.add(Arrays.asList("Token", "", "Current price", "Amount", "Total amount in USDT", "Price change"));
        walletInfo.forEach(token -> {
            tokensList.add(Arrays.asList(token.getSymbol(),
                    "=IMAGE(\"" + token.getImage() + "\")",
                    token.getCurrentPrice(),
                    token.getAmount(),
                    token.getTotalAmountInUSDT(),
                    token.getPriceChangePercentage24h() + "%"));
        });
        tokensList.add(Arrays.asList("", "", "", "", "=SUM(E2:E" + (walletInfo.size() + 1) + ")", ""));

        ClearValuesRequest requestBody = new ClearValuesRequest();
        ClearValuesResponse execute = sheets.spreadsheets().values().clear(spreadsheetId, (sheet + "!A1:P" + walletInfo.size() + 1), requestBody)
                .execute();
        System.out.println(execute);

        List<Request> requests = new ArrayList<>();
        Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();
        spreadsheet.getSheets().forEach(s -> {
            requests.add(new Request().setUpdateDimensionProperties(
                    new UpdateDimensionPropertiesRequest()
                            .setRange(new DimensionRange()
                                    .setSheetId(s.getProperties().getSheetId())
                                    .setDimension("COLUMNS")
                                    .setStartIndex(1)
                                    .setEndIndex(2)
                            )
                            .setProperties(new DimensionProperties().setPixelSize(20)
                            )
                            .setFields("pixelSize")));
            BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            try {
                BatchUpdateSpreadsheetResponse response = sheets.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ValueRange body = new ValueRange()
                .setValues(tokensList
                );
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, sheet + "!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute();
        System.out.println(result);

    }

    public String getExcelUrl() {
        Spreadsheet spreadsheet = null;
        try {
            spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spreadsheet != null ? spreadsheet.getSpreadsheetUrl() : "";

    }

    public String getEnrichedString(String name, int neededLength) {
        int length = name.length();
        return length < neededLength
                ? name.concat(" ".repeat(neededLength - length))
                : name.substring(0, neededLength);
    }
}
