package com.test;

import com.pivotallabs.chicago.pact.provider.spring.mockmvc.PactProviderVerifier;
import org.junit.Test;

public class ProviderTest {
    @Test
    public void verifiesControllerActionsDefinedInSpecifiedPact() throws Exception {
        String pactFileLocation =
                "/Users/markducommun/src/pact_provider_spring_mock_mvc/test/target/pacts/test_consumer-test_provider_success.json";

        TestController testController = new TestController();

        PactProviderVerifier.verifyPact(pactFileLocation, testController);
    }

    @Test
    public void controllerActionRespondingWith401() throws Exception {
        String pactFileLocation =
                "/Users/markducommun/src/pact_provider_spring_mock_mvc/test/target/pacts/test_consumer-test_provider_failure.json";

        TestController testController = new TestController();

        PactProviderVerifier.verifyPact(pactFileLocation, testController);
    }
}
