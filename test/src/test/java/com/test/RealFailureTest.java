package com.test;

import com.pivotallabs.chicago.pact.provider.spring.mockmvc.PactProviderVerifier;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.util.AssertionErrors.fail;

public class RealFailureTest {


    private TestService testService;

    @Before
    public void setUp() throws Exception {
        testService = mock(TestService.class);
    }

    @Test
    public void failsWhenHeaderIsWrong() throws Exception {
        String pactFileLocation =
                "target/pacts/test_consumer-test_provider_success.json";

        try {
            PactProviderVerifier.verifyPact(pactFileLocation, createPactTestController(() ->
                    ResponseEntity
                            .status(201) // Think that we at least need a status
                            .header("Foo", "WIBBLEWOBBLE")
                            .build()
            ));
            fail("expected exception");
        } catch (AssertionError e) {
            assertThat(e.getMessage()).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'");
        }
    }

    @Test
    public void failsWhenHeaderIsWrongFromString() throws Exception {
        try {
            String pactString = "{ \"request\": { \"method\": \"POST\", \"path\": \"/login\"}, \"response\": { \"status\": 201, \"headers\": { \"Foo\": \"Bar\"}, \"matchingRules\": { \"$.headers.Foo\": { \"regex\": \"Bar\" }} } }";

            PactProviderVerifier.verifyPactFromString(pactString, createPactTestController(() ->
                    ResponseEntity
                            .status(201)
                            .header("Foo", "WIBBLEWOBBLE")
                            .build()
            ));
            fail("expected exception");
        } catch (AssertionError e) {
            assertThat(e.getMessage()).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'");
        }
    }

    @Test
    public void failsWhenHeaderIsWrongFromStringFromJSON() throws Exception {
        try {
            JSONObject interactionJSON = new JSONObject()
                    .put("request", new JSONObject()
                            .put("method", "POST")
                            .put("path", "/baz"))
                    .put("response", new JSONObject()
                            .put("status", 201)
                            .put("headers", new JSONObject()
                                    .put("Foo", "Bar"))
                            .put("matchingRules", new JSONObject()
                                    .put("$.headers.Foo", new JSONObject()
                                            .put("regex", "Bar"))));

            String pactString = interactionJSON.toString();

            PactProviderVerifier.verifyPactFromString(pactString, createPactTestController(() ->
                    ResponseEntity
                            .status(201)
                            .header("Foo", "WIBBLEWOBBLE")
                            .build()
            ));
            fail("expected exception");
        } catch (AssertionError e) {
            assertThat(e.getMessage()).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'");
        }
    }


    @Test
    public void failsWhenHeaderIsMissing() throws Exception {
        String pactFileLocation =
                "target/pacts/test_consumer-test_provider_success.json";

        try {
            PactProviderVerifier.verifyPact(pactFileLocation, createPactTestController(() -> ResponseEntity
                    .status(201)
                    .header("Foo", "WIBBLEWOBBLE")
                    .build()
            ));
            fail("expected exception");
        } catch (AssertionError e) {
            assertThat(e.getMessage()).contains("Expected a header 'Content-Type' but was missing");
        }
    }

    @Test
    public void failsWhenStatusIsWrongButWithOurFancyTestRig() throws Exception {
        String pactFileLocation =
                "target/pacts/test_consumer-test_provider_success.json";

        try {
            PactProviderVerifier.verifyPact(pactFileLocation, createPactTestController(() -> ResponseEntity
                    .status(400)
                    .header("Foo", "Bar")
                    .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .build()
            ));
            fail("expected exception");
        } catch (AssertionError e) {
            assertThat(e.getMessage()).contains("400");
            assertThat(e.getMessage()).contains("201");
        }
    }

    public static Object createPactTestController(Supplier<ResponseEntity> controllerMethodBody) {
        return new AbstractPactTestController() {
            @Override
            public ResponseEntity controllerMethod() {
                return controllerMethodBody.get();
            }
        };
    }

    @RestController
    public static abstract class AbstractPactTestController {
        @RequestMapping("/**")
        public abstract ResponseEntity controllerMethod();
    }
}
