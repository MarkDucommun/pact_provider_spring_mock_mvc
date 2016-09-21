import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.pivotallabs.chicago.pact.provider.spring.mockmvc.PactProviderVerifier
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.util.AssertionErrors.fail
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

class PactProviderVerifierTest {

    @Test
    fun verifyPact_sendsARequestWithTheSpecifiedMethod() {
        val interaction = interaction {
            request {
                method = "POST"
                path = "/anyPath"
            }
            response {
                status = 204
            }
        }

        verifyPactFromInteraction(interaction, createPactTestController { request ->
            assertThat(request.method).isEqualTo(HttpMethod.POST)

            ResponseEntity
                    .noContent()
                    .build()
        })
    }

    @Test
    fun verifyPact_sendsARequestWithTheSpecifiedPath() {
        val interaction = interaction {
            request {
                method = "POST"
                path = "/specifiedPath"
            }
            response {
                status = 204
            }
        }

        verifyPactFromInteraction(interaction, createPactTestController { request ->
            assertThat(request.url.path).isEqualTo("/specifiedPath")

            ResponseEntity
                    .noContent()
                    .build()
        })
    }

    @Test
    fun verifyPact_sendsARequestWithSpecifiedHeaders() {
        val interaction = interaction {
            request {
                method = "POST"
                path = "/specifiedPath"
                headers {
                    header("Foo", "Bar")
                }
            }
            response {
                status = 204
            }
        }

        verifyPactFromInteraction(interaction, createPactTestController { request ->
            val fooHeader: MutableList<String>? = request.headers.get("Foo")
            assertThat(fooHeader).isEqualTo(mutableListOf("Bar"))

            ResponseEntity
                    .noContent()
                    .build()
        })
    }

    @Test
    fun verifyPact_failsWhenStatusIsWrong() {
        try {
            val interaction = interaction {
                request {
                    method = "POST"
                    path = "/anyPath"
                }
                response {
                    status = 204
                }
            }

            verifyPactFromInteraction(interaction, createPactTestController({
                ResponseEntity
                        .status(400)
                        .build()
            }))

            fail("expected exception")
        } catch (e: AssertionError) {
            assertThat(e.message).contains("204")
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
        abstract fun controllerMethod(request : RequestEntity<*>): ResponseEntity<*>
    }

    companion object {
        fun createPactTestController(controllerMethodBody: (request: RequestEntity<*>) -> ResponseEntity<*>): Any {
            return object : AbstractPactTestController() {
                override fun controllerMethod(request: RequestEntity<*>): ResponseEntity<*> {
                    return controllerMethodBody.invoke(request)
                }
            }
        }
    }
}

class PactInteraction() {
    abstract class PactEntity() {
        var headers = hashMapOf<String, String>()

        fun headers(init: PactEntity.() -> Unit) {
            init()
        }

        fun header(name: String, value: String) {
            headers.put(name, value)
        }

        var matchingRules = hashMapOf<String, HashMap<String, String>>()

        fun matchingRules(init: PactEntity.() -> Unit) {
            init()
        }

        fun regexRule(jsonPath: String, regex: String) {
            matchingRules.put(jsonPath, hashMapOf<String, String>(Pair("regex", regex)))
        }
    }

    class PactRequest() : PactEntity() {
        lateinit var method: String
        lateinit var path: String
    }

    class PactResponse() : PactEntity() {
        var status = 200
    }

    lateinit var request: PactRequest
    lateinit var response: PactResponse

//    protected fun <T : PactEntity> initTag(pactEntity: T, init: T.() -> Unit): T {
//        tag.init()
//        children.add(tag)
//        return tag
//    }

    // TODO generify these two functions
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