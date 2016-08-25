package com.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

public class TestClient {
    private String baseUrl;

    public TestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String login(String username, String password) throws IOException {
        ImmutableMap<String, String> request = ImmutableMap.of("username", username, "password", password);

        RestTemplate restTemplate = new RestTemplate();

        String resultString = restTemplate.postForObject(baseUrl + "/login", request, String.class);

        JsonNode result = new ObjectMapper().readValue(resultString, JsonNode.class);

        return result.get("token").textValue();
    }
}
