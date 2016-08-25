package com.test;


import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ConsumerTest {
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("test_provider", "localhost", 8080, this);

    @Pact(consumer="test_consumer")
    public PactFragment createValidFragment(PactDslWithProvider builder) {
        return builder
                .given("valid_credentials")
                .uponReceiving("request to create Token")
                .path("/login")
                .body("{ \"username\": \"username\", \"password\": \"valid-password\" }")
                .method("POST")

                .willRespondWith()
                .status(201)
                .body("{\"token\": \"A12345\"}")
                .toFragment();
    }

    @Test
    @PactVerification
    public void runValidTest() throws IOException {
        String token = new TestClient("http://localhost:8080").login("username", "valid-password");

        assertThat(token).isEqualTo("A12345");
    }
}
