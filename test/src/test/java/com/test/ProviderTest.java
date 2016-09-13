package com.test;

import com.pivotallabs.chicago.pact.provider.spring.mockmvc.PactProviderVerifier;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ProviderTest {


    private TestService testService;

    @Before
    public void setUp() throws Exception {
        testService = mock(TestService.class);
    }

    @Test
    public void verifiesControllerActionsDefinedInSpecifiedPact() throws Exception {
        String pactFileLocation =
                "target/pacts/test_consumer-test_provider_success.json";

        doReturn("blah").when(testService).getToken(anyString(), anyString());

        TestController testController = new TestController(testService);

        PactProviderVerifier.verifyPact(pactFileLocation, testController);
    }

    @Test
    public void controllerActionRespondingWith401() throws Exception {
        String pactFileLocation =
                "target/pacts/test_consumer-test_provider_failure.json";

        TestController testController = new TestController(testService);

        PactProviderVerifier.verifyPact(pactFileLocation, testController);
    }
}
