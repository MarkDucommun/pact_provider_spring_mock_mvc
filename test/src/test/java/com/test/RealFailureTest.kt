import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.pivotallabs.chicago.pact.provider.spring.mockmvc.PactProviderVerifier
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.json.JSONObject
import org.junit.Test
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.test.util.AssertionErrors.fail
import java.util.*

class RealFailureTest {
    @Test
    fun failsWhenHeaderIsWrongFromString() {
        try {
            val pactString = "{ \"request\": { \"method\": \"POST\", \"path\": \"/login\"}, \"response\": { \"status\": 201, \"headers\": { \"Foo\": \"Bar\"}, \"matchingRules\": { \"$.headers.Foo\": { \"regex\": \"Bar\" }} } }"

            PactProviderVerifier.verifyPactFromString(pactString, createPactTestController({
                ResponseEntity
                        .status(201)
                        .header("Foo", "WIBBLEWOBBLE")
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'")
        }
    }

    @Test
    fun failsWhenHeaderIsWrongFromMultilineString() {
        try {
            val pactString = """
{
    "request": {
        "method": "POST",
        "path": "/login"
    },
    "response": {
        "status": 201,
        "headers": {
            "Foo": "Bar"
        },
        "matchingRules": {
            "$.headers.Foo": {
                "regex": "Bar"
            }
        }
    }
}
            """

            PactProviderVerifier.verifyPactFromString(pactString, createPactTestController({
                ResponseEntity
                        .status(201)
                        .header("Foo", "WIBBLEWOBBLE")
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'")
        }
    }

    @Test
    fun failsWhenHeaderIsWrongFromStringFromJSON() {
        try {
            val interactionJSON = JSONObject()
                    .put("request", JSONObject()
                            .put("method", "POST")
                            .put("path", "/baz"))
                    .put("response", JSONObject()
                            .put("status", 201)
                            .put("headers", JSONObject()
                                    .put("Foo", "Bar"))
                            .put("matchingRules", JSONObject()
                                    .put("$.headers.Foo", JSONObject()
                                            .put("regex", "Bar"))))

            val pactString = interactionJSON.toString()

            PactProviderVerifier.verifyPactFromString(pactString, createPactTestController({
                ResponseEntity
                        .status(201)
                        .header("Foo", "WIBBLEWOBBLE")
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'")
        }
    }

    @Test
    fun failsWhenHeaderIsWrongFromStringFromKotlinBuilder() {
        try {
            val interaction = interaction {
                request {
                    method = "POST"
                    path = "/anyPath"
                }
                response {
                    status = 201
                    // TODO can we do better?
//                    headers {
//                        + header("Foo", "Bar")
//                    }
                    headers = hashMapOf(Pair(
                            "Foo",
                            "Bar"))
//                    matchingRules {
//                        + rule("$.headers.Foo", )
//                    }
                    matchingRules = hashMapOf(Pair(
                            "$.headers.Foo",
                            hashMapOf(Pair(
                                    "regex",
                                    "Bar"))))
                }
            }

            verifyPactFromInteraction(interaction, createPactTestController({
                ResponseEntity
                        .status(201)
                        .header("Foo", "WIBBLEWOBBLE")
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected 'WIBBLEWOBBLE' to match 'Bar'")
        }
    }


    @Test
    fun failsWhenHeaderIsMissing() {
        val pactFileLocation = "target/pacts/test_consumer-test_provider_success.json"

        try {
            PactProviderVerifier.verifyPact(pactFileLocation, createPactTestController({
                ResponseEntity
                        .status(201)
                        .header("Foo", "WIBBLEWOBBLE")
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected a header 'Content-Type' but was missing")
        }

    }

    @Test
    fun failsWhenStatusIsWrongButWithOurFancyTestRig() {
        val pactFileLocation = "target/pacts/test_consumer-test_provider_success.json"

        try {
            PactProviderVerifier.verifyPact(pactFileLocation, createPactTestController({
                ResponseEntity
                        .status(400)
                        .header("Foo", "Bar")
                        .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("400")
            assertThat(e.message).contains("201")
        }

    }

    @RestController
    abstract class AbstractPactTestController {
        @RequestMapping("/**")
        abstract fun controllerMethod(): ResponseEntity<*>
    }

    companion object {
        fun createPactTestController(controllerMethodBody: () -> ResponseEntity<*>): Any {
            return object : AbstractPactTestController() {
                override fun controllerMethod(): ResponseEntity<*> {
                    return controllerMethodBody.invoke()
                }
            }
        }
    }
}

class Header(val fieldName : String, val fieldValue : String ) {
}

class PactRequest() {
    lateinit var method : String
    lateinit var path : String
}

class PactResponse() {
    var status = 200
//    lateinit var headers : HashMap<String, String>
    var headers = hashMapOf<String, String>()
    lateinit var matchingRules : HashMap<String, HashMap<String, String>>
}

class PactInteraction() {
    lateinit var request : PactRequest
    lateinit var response : PactResponse

    fun request(init: PactRequest.() -> Unit): Unit {
        val pactRequest = PactRequest()
        pactRequest.init()
        request = pactRequest
    }

    fun response(init: PactResponse.() -> Unit): Unit {
        val pactResponse = PactResponse()
        pactResponse.init()
        response = pactResponse
    }
}

fun interaction(init: PactInteraction.() -> Unit): PactInteraction {
    val pactInteraction = PactInteraction()
    pactInteraction.init()
    return pactInteraction
}

fun verifyPactFromInteraction(interaction: PactInteraction, controller: Any) {
    val objectMapper = ObjectMapper()
    objectMapper.registerModule(KotlinModule())
    val pactString = objectMapper.writeValueAsString(interaction)

    PactProviderVerifier.verifyPactFromString(pactString, controller)
}