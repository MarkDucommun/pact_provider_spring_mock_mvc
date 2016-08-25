package com.test;

import com.pivotallabs.chicago.pact.provider.spring.mockmvc.PactProviderVerifier;
import org.junit.Test;

public class ProviderTest {
    @Test
    public void verifiesControllerActionsDefinedInSpecifiedPact() throws Exception {
        String pactFileLocation =
                "/Users/markducommun/src/authentication_project/target/pacts/test_login_consumer-test_authentication_provider.json";

        TestController testController = new TestController();

        PactProviderVerifier.verifyPact(pactFileLocation, testController);
    }
}
