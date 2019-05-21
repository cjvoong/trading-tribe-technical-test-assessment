import io.restassured.RestAssured
import io.restassured.config.SSLConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance (TestInstance.Lifecycle.PER_CLASS)
class FixturesApiTest {

    @BeforeAll
    fun setup(){
        val cfg = SSLConfig()
            .allowAllHostnames()
            .relaxedHTTPSValidation()
        RestAssured.config = RestAssured.config().sslConfig(cfg)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        RestAssured.baseURI = "http://localhost:3000"
    }

    @Test
    fun testNoFixtures(){
        val count = RestAssured.given().expect().statusCode(200).`when`().get("fixtures").then().extract().response().
            jsonPath().getInt("$.size()")
        assertEquals(3,count)
    }

    @Test
    fun testEachFixtureHasFixtureId(){
        val fixtureIds = RestAssured.given().expect().statusCode(200).`when`().get("fixtures").then().extract().response().jsonPath().getList<Int>("fixtureId")
        assertEquals(4,fixtureIds.size)
    }

    @Test
    fun testGetNewFixture(){
        val teamId = RestAssured.given().expect().statusCode(200).`when`().get("fixture/4").then().extract().response().jsonPath().getString("footballFullState.teams[0].teamId")
        assertEquals("HOME",teamId)
    }

    @Test
    fun testCreateAndGetFixture(){

    }

    @Test
    fun testCreateAndDeleteFixture(){

    }


    @Test
    fun failedTest(){
        assertTrue(false)
    }
}