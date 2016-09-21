package com.pivotallabs.chicago.pact.provider.spring.mockmvc;

import au.com.dius.pact.model.*;
import au.com.dius.pact.provider.ResponseComparison;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PactProviderVerifier {
    public static void verifyPactFromString(String pactString, Object controller) {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        try {
            RequestResponseInteraction requestResponseInteraction = new ObjectMapper().readValue(pactString, RequestResponseInteraction.class);

            Request request = requestResponseInteraction.getRequest();
            Response response = requestResponseInteraction.getResponse();

            ResultActions resultActions = performRequest(mockMvc, request);

            verifyResponse(resultActions, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void verifyPact(String pactFileLocation, Object controller) {
        List<Interaction> interactions = loadPacts(pactFileLocation);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        interactions.forEach(interaction -> {
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
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE); // TODO why is this hardcoded?
        }

        return mockMvcRequest;
    }

    private static void verifyResponse(ResultActions resultActions, Response response) throws Exception {
        resultActions.andDo(new ResultHandler() {
            @Override
            public void handle(MvcResult result) throws Exception {
                MockHttpServletResponse actualResponse = result.getResponse();

                ImmutableMap<String, String> mimeType = ImmutableMap.of("mimeType", "mimeTypeType");
                ImmutableMap<String, Object> actual = ImmutableMap.of(
                        "contentType", mimeType,
                        "statusCode", actualResponse.getStatus(),
                        "data", actualResponse.getContentAsString());

                HashMap<Object, Object> headers = new HashMap<>();

                actualResponse.getHeaderNames().forEach(name -> {
                    headers.put(name, actualResponse.getHeaders(name).stream().collect(Collectors.joining(", ")));
                });

                Map missmatches = (Map) ResponseComparison.compareResponse(response, actual, actualResponse.getStatus(), headers, actualResponse.getContentAsString());
                // TODO: iterate missmatches and print the right thing
                // TODO: something is wrong with headers

                Object statusValidity = missmatches.get("method");
                if (statusValidity instanceof PowerAssertionError) {
                    throw (PowerAssertionError) statusValidity;
                }

                LinkedHashMap<String, Object> headersValidityResponse = (LinkedHashMap) missmatches.get("headers");
                for (String headerKey : headersValidityResponse.keySet()) {
                    Object headerValidity = headersValidityResponse.get(headerKey);

                    if (headerValidity instanceof String) {
                        throw new AssertionError(headerValidity);
                    }

//                    try {
//                        String errorMessage = (String) headersValidityResponse.get(headerKey);
//
//                    } catch (ClassCastException e) {
//                    }
                }

                LinkedHashMap bodyValidityResponse = (LinkedHashMap) missmatches.get("body");

                System.out.printf("");
            }
        });
    }
}
