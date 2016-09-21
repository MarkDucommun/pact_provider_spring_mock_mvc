package com.test;


import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class ConsumerTest_success {
    public static final String TOKEN = "A12345";
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("test_provider_success", "localhost", 8080, this);

    @Pact(consumer="test_consumer")
    public PactFragment createValidFragment(PactDslWithProvider builder) {
        DslPart requestBody = new PactDslJsonBody().stringType("username").stringType("password").close();
        DslPart responseBody = new PactDslJsonBody().stringMatcher("token", ".*", TOKEN).close();

        return builder
                .given("valid_credentials")
                .uponReceiving("request to create Token")
                .path("/login")
                .matchHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .matchHeader("Baz", "Bar")
                .body(requestBody)
                .method("POST")

                .willRespondWith()
                .status(201)
                .matchHeader("Foo", "Bar")
                .matchHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                .body(responseBody)
                .toFragment();
    }

    @Test
    @PactVerification
    public void runValidTest() throws IOException {
        String token = new TestClient("http://localhost:8080").login("username", "valid-password");

        assertThat(token).isEqualTo(TOKEN);
    }
}
