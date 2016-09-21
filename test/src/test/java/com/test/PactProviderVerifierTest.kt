import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jayway.jsonpath.JsonPath
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

class PactProviderVerifierTest {

    @Test
    fun verifyPact_failsWhenStatusIsWrong() {
        try {
            val interaction = interaction {
                request {
                    method = "POST"
                    path = "/anyPath"
                }
                response {
                    status = 201
                }
            }

            verifyPactFromInteraction(interaction, createPactTestController({
                ResponseEntity
                        .status(400)
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("201")
            assertThat(e.message).contains("400")
        }

    }

    @Test
    fun verifyPact_failsWhenHeaderIsWrong() {
        try {
            val interaction = interaction {
                request {
                    method = "POST"
                    path = "/anyPath"
                }
                response {
                    status = 201
                    headers {
                        header("Foo", "Bar")
                    }
                    matchingRules {
                        regexRule("$.headers.Foo", "Bar")
                    }
                }
            }

            verifyPactFromInteraction(interaction, createPactTestController({
                ResponseEntity
                        .status(201)
                        .header("Foo", "Baz")
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected 'Baz' to match 'Bar'")
        }
    }

    @Test
    fun verifyPact_failsWhenHeaderIsMissing() {
        try {
            val interaction = interaction {
                request {
                    method = "POST"
                    path = "/anyPath"
                }
                response {
                    status = 201
                    headers {
                        header("Foo", "Bar")
                    }
                    matchingRules {
                        regexRule("$.headers.Foo", "Bar")
                    }
                }
            }

            verifyPactFromInteraction(interaction, createPactTestController({
                ResponseEntity
                        .status(201)
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("Expected a header 'Foo' but was missing")
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

class PactInteraction() {
    class PactRequest() {
        lateinit var method: String
        lateinit var path: String
    }

    class PactResponse() {
        var status = 200
        var headers = hashMapOf<String, String>()
        var matchingRules = hashMapOf<String, HashMap<String, String>>()

        fun headers(init: PactResponse.() -> Unit) {
            init()
        }

        fun header(name: String, value: String) {
            headers.put(name, value);
        }

        fun matchingRules(init: PactResponse.() -> Unit) {
            init()
        }

        fun regexRule(jsonPath: String, regex: String) {
            matchingRules.put(jsonPath, hashMapOf<String, String>(Pair("regex", regex)))
        }
    }

    lateinit var request: PactRequest
    lateinit var response: PactResponse

    fun request(init: PactRequest.() -> Unit) {
        val pactRequest = PactRequest()
        pactRequest.init()
        request = pactRequest
    }

    fun response(init: PactResponse.() -> Unit) {
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