package com.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

public class TestClient {
    private String baseUrl;

    public TestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String login(String username, String password) throws IOException {
        ImmutableMap<String, String> request = ImmutableMap.of("username", username, "password", password);

        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = baseUrl + "/login";
            RequestEntity<ImmutableMap<String, String>> requestEntity = RequestEntity.post(URI.create(url))
                    .header("Baz", "Bar")
                    .body(request);

            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

            String resultString = responseEntity.getBody();

            JsonNode result = new ObjectMapper().readValue(resultString, JsonNode.class);

            return result.get("token").textValue();
        } catch (HttpClientErrorException e) {
            return null;
        }
    }
}
