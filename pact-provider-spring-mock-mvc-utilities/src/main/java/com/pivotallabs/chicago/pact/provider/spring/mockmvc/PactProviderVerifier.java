package com.pivotallabs.chicago.pact.provider.spring.mockmvc;

import au.com.dius.pact.model.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

public class PactProviderVerifier {
    public static void verifyPact(String pactFileLocation, Object controller) {
        List<Interaction> interactions = loadPacts(pactFileLocation);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        interactions.stream().forEach(interaction -> {
            try {
                RequestResponseInteraction requestResponseInteraction = (RequestResponseInteraction) interaction;
                Request request = requestResponseInteraction.getRequest();
                Response response = requestResponseInteraction.getResponse();

                ResultActions resultActions = performRequest(mockMvc, request);

                verifyResponse(resultActions, response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static List<Interaction> loadPacts(String pactFileLocation) {
        Pact pact = PactReader.loadPact(pactFileLocation);
        return pact.getInteractions();
    }

    private static ResultActions performRequest(MockMvc mockMvc, Request request) throws Exception {
        MockHttpServletRequestBuilder mockMvcRequest = createMockMvcRequest(request);

        return mockMvc.perform(mockMvcRequest);
    }

    private static MockHttpServletRequestBuilder createMockMvcRequest(Request request) {
        MockHttpServletRequestBuilder mockMvcRequest = MockMvcRequestBuilders.request(
                HttpMethod.resolve(request.getMethod()),
                request.getPath()
        );

        if (request.getBody().isPresent()) {
            mockMvcRequest = mockMvcRequest
                    .content(request.getBody().getValue())
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        }

        return mockMvcRequest;
    }

    private static void verifyResponse(ResultActions resultActions, Response response) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().is(response.getStatus()));
        resultActions.andExpect(MockMvcResultMatchers.content().string(response.getBody().getValue()));
        response.getHeaders().forEach((headerType, headerValue) -> {
            try {
                resultActions
                        .andExpect(MockMvcResultMatchers.header()
                                .stringValues(headerType, headerValue));
            } catch (Exception e) {
            }
        });
    }
}
