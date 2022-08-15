package com.forestik.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleApiConfiguration {

    /**
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final String tokensDirectoryPath;
    private final String clientId;
    private final String authUri;
    private final String tokenUri;
    private final String clientSecret;
    private final String redirectUris;
    private final String serverPort;

    public GoogleApiConfiguration(@Value("${google.token.path}") String tokensDirectoryPath,
                                  @Value("${google.client.id}") String clientId,
                                  @Value("${google.auth.uri}") String authUri,
                                  @Value("${google.token.uri}") String tokenUri,
                                  @Value("${google.client.secret}") String clientSecret,
                                  @Value("${google.redirect.uris}") String redirectUris,
                                  @Value("${server.port}") String serverPort) {
        this.tokensDirectoryPath = tokensDirectoryPath;
        this.clientId = clientId;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris;
        this.serverPort = serverPort;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     */
    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                .setClientId(clientId)
                .setAuthUri(authUri)
                .setTokenUri(tokenUri)
                .setClientSecret(clientSecret)
                .setRedirectUris(Collections.singletonList(redirectUris));
        clientSecrets.setWeb(details);
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(Integer.parseInt(serverPort)).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Bean
    public Sheets getSheets() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport)).setApplicationName("Portfolio")
                .build();
    }
}
