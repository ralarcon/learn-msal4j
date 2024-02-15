package com.ragcdev.learn.msal4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CheckedInputStream;

import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import com.fasterxml.jackson.databind.ObjectMapper;



public class Main {

  final static String CLIENT_ID = "<CLIENT_ID>";
  final static String SECRET = "<CLIENT_SECRET>";

  final static String USER = "<OAUTH_USER>";
  final static String PASSWORD = "<OAUTH_USER_PASSWORD>";

  final static String AUTHORITY = "https://login.microsoftonline.com/<TENANT_ID>";
  final static String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";
  final static String CUSTOM_WEBAPI_SCOPE = "<CUSTOM_WEBAPI_SCOPE>";
  final static HttpClient httpClient = buildHttpClient();


  public static void main(String[] args) {

    System.out.println("Hello and welcome!");

    try {
      System.out.println("Creating confidential client application...");
      final ConfidentialClientApplication confidentialClient = buildConfidentialClient(CLIENT_ID, SECRET, AUTHORITY);

      InvokeSecuredApiEndpoint(confidentialClient, "https://graph.microsoft.com/v1.0/users", GRAPH_DEFAULT_SCOPE);

      InvokeSecuredApiEndpoint(confidentialClient, "http://localhost:8082/api/date/appRole", CUSTOM_WEBAPI_SCOPE);

      Map<String, String> headers = Map.of("x-data-id", "1", "x-data2-id", "1");
      InvokeSecuredApiEndpoint("https://localhost:8082/v1/containers", USER, PASSWORD, headers);


    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  private static HttpClient buildHttpClient() {
    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();
  }

  private static ConfidentialClientApplication buildConfidentialClient(String clientId, String secret, String authority) throws Exception {

    // Load properties file and set properties used throughout the sample
    final ConfidentialClientApplication app;
    app = ConfidentialClientApplication.builder(
            clientId,
            ClientCredentialFactory.createFromSecret(secret))
        .authority(authority)
        .build();
    return app;
  }
  private static void InvokeSecuredApiEndpoint(ConfidentialClientApplication confidentialClient, String endpoint, String scope) throws IOException {
    System.out.println("Sending HTTP GET request to " + endpoint + "...");
    final HTTPResponse response = sendHttpRequestBearerAuthz(endpoint, scope, confidentialClient);
    printResponse(response);
  }

  private static void InvokeSecuredApiEndpoint(String endpoint, String user, String password, Map<String, String> headers) throws IOException {
    System.out.println("Sending HTTP GET request to " + endpoint + "...");
    final HTTPResponse response = sendHttpRequestBasicAuthz(endpoint, user, password, headers);
    printResponse(response);
  }

  private static HTTPResponse sendHttpRequestBearerAuthz(String url, String scope, ConfidentialClientApplication clientApp) throws IOException {
    try {
      // We need to getTokenEveryTime because the token expires after a certain amount of time, if valid will be taken from cache
      IAuthenticationResult authenticationResult = getTokenForApp(clientApp, scope);

      HTTPRequest request = new HTTPRequest(HTTPRequest.Method.GET, URI.create(url));
      request.setAuthorization("Bearer " + authenticationResult.accessToken());
      request.setHeader("Accept", "*/*");
      return request.send();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static HTTPResponse sendHttpRequestBasicAuthz(String url, String username, String password, Map<String, String> headers) throws IOException {
  try {
    String authzHeader = generateBasicAuthHeader(username, password);

    HTTPRequest request = new HTTPRequest(HTTPRequest.Method.GET, URI.create(url));
    request.setAuthorization(authzHeader);
    request.setHeader("Accept", "*/*");

    if (headers != null) {
      for (Map.Entry<String, String> header : headers.entrySet()) {
        request.setHeader(header.getKey(), header.getValue());
      }
    }

    return request.send();

  } catch (Exception e) {
    throw new RuntimeException(e);
  }
}


  public static String generateBasicAuthHeader(String username, String password) {
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
    return "Basic " + new String(encodedAuth);
  }

  private static IAuthenticationResult getTokenForApp(ConfidentialClientApplication app, String scope) throws Exception {
    final var clientCredentialParameters = ClientCredentialParameters.builder(
            Collections.singleton(scope))
        .build();
    CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParameters);
    return future.get();
  }

  private static void printResponse(HTTPResponse response) {
    if (response!=null && response.getStatusCode() >= 400) {
      String statusInfo = response.getStatusMessage() != null ?
          MessageFormat.format("{0}: {1}", response.getStatusCode(), response.getStatusMessage()) : String.valueOf(response.getStatusCode());
      System.out.println("Request failed with status code " + statusInfo);
    }

    if(response==null || response.getContent() == null) {
      System.out.println("Response: (empty)");
    }
    else {
      String contentType = response.getHeaderValue("Content-Type");
      if (contentType != null && contentType.contains("application/json")) {
        ObjectMapper mapper = new ObjectMapper();
        try {
          Object json = mapper.readValue(response.getContent(), Object.class);
          String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
          System.out.println("Response: ");
          System.out.println(prettyJson);
        } catch (Exception e) {
          System.out.println("Error parsing JSON response: " + e.getMessage());
        }
      } else {
        System.out.println("Response: " + response.getContent());
      }
    }
    System.out.println();
  }
}
