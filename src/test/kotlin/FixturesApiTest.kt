import io.restassured.RestAssured
import io.restassured.config.SSLConfig
import io.restassured.http.ContentType
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.Thread.sleep
import kotlin.reflect.KClass

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

        //add new fixture
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
        assertEquals(3,fixtureIds.size)
    }

    @Test
    fun testGetNewFixture(){
        //add new fixture
        val expected = Fixture(
            "4",
            FixtureStatus(true,true),
            FootballFullState(
                "Baps",
                "Rolls",
                false,
                0,
                emptyList(),
                "SECOND_HALF",
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                "2018-03-20T10:49:38.655Z",
                true,
                listOf(
                    Team("HOME","Baps","HOME"),
                    Team("AWAY","Rolls","AWAY")
                )
            )
        )

        //post object
        RestAssured.given().contentType(ContentType.JSON).body(expected).expect().statusCode(200).`when`().post("fixture").then().assertThat().body(equalTo("Fixture has been added"))

        //get new fixture and check teamId
        val teamId = RestAssured.given().expect().statusCode(200).`when`().get("fixture/${expected.fixtureId}").then().extract().response().jsonPath().getString("footballFullState.teams[0].teamId")
        assertEquals("HOME",teamId)

        //delete created data
        RestAssured.given().body(expected).expect().statusCode(200).`when`().delete("fixture/${expected.fixtureId}").then().assertThat().body(equalTo("Fixture has been deleted"))
    }

    @Test
    fun testCreateAndGetFixture(){
        val expected = Fixture(
            "5",
            FixtureStatus(true,true),
            FootballFullState(
                "Baps",
                "Rolls",
                false,
                0,
                emptyList(),
                "SECOND_HALF",
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                "2018-03-20T10:49:38.655Z",
               true,
                listOf(
                    Team("HOME","Baps","HOME"),
                    Team("AWAY","Rolls","AWAY")
                )
            )
        )

        //post object
        RestAssured.given().contentType(ContentType.JSON).body(expected).expect().statusCode(200).`when`().post("fixture").then().assertThat().body(equalTo("Fixture has been added"))

        //get object
        var counter = 10
        var actual:Fixture
        do {
            val response = RestAssured.given().`when`().get("fixture/${expected.fixtureId}").then().extract().response()
            actual = RestAssured.given().`when`().get("fixture/${expected.fixtureId}").then().extract().`as`(Fixture::class.java)
            counter--
            sleep(500)

        } while ((response.statusCode != 200) && (counter > 0))
        assertEquals(expected,actual)

        //delete created data
        RestAssured.given().body(expected).expect().statusCode(200).`when`().delete("fixture/${expected.fixtureId}").then().assertThat().body(equalTo("Fixture has been deleted"))
    }


    @Test
    fun testCreateAndDeleteFixture(){
        val expected = Fixture(
            "6",
            FixtureStatus(true,false),
            FootballFullState(
                "Baps",
                "Rolls",
                false,
                0,
                emptyList(),
                "SECOND_HALF",
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                "2018-03-20T10:49:38.655Z",
                true,
                listOf(
                    Team("HOME","Baps","HOME"),
                    Team("AWAY","Rolls","AWAY")
                )
            )
        )

        //post object
        RestAssured.given().contentType(ContentType.JSON).body(expected).expect().statusCode(200).`when`().post("fixture").then().assertThat().body(equalTo("Fixture has been added"))

        var counter = 10
        var actual:Fixture
        do {
            val response = RestAssured.given().`when`().get("fixture/${expected.fixtureId}").then().extract().response()
            actual = RestAssured.given().`when`().get("fixture/${expected.fixtureId}").then().extract().`as`(Fixture::class.java)
            counter--
            sleep(500)

        } while ((response.statusCode != 200) && (counter > 0))

        assertEquals(expected,actual)

        //delete object
        RestAssured.given().body(expected).expect().statusCode(200).`when`().delete("fixture/${expected.fixtureId}").then().assertThat().body(equalTo("Fixture has been deleted"))

        //check object is no longer there
        RestAssured.given().expect().statusCode(404).`when`().get("fixture/${expected.fixtureId}")

    }

}

data class Fixture (
    val fixtureId: String,
    val fixtureStatus: FixtureStatus,
    val footballFullState: FootballFullState
)

data class FixtureStatus (
    val displayed: Boolean,
    val suspended: Boolean
)

data class FootballFullState (
    val homeTeam: String,
    val awayTeam: String,
    val finished: Boolean,
    val gameTimeInSeconds: Long,
    val goals: List<Any?>,
    val period: String,
    val possibles: List<Any?>,
    val corners: List<Any?>,
    val redCards: List<Any?>,
    val yellowCards: List<Any?>,
    val startDateTime: String,
    val started: Boolean,
    val teams: List<Team>
)

data class Team (
    val association: String,
    val name: String,
    val teamId: String
)
