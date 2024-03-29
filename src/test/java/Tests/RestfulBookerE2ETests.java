package Tests;

import DataModel.BookingData;
import DataModel.PartialBookingData;
import DataModel.TokenCreds;
import com.github.javafaker.Faker;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static DataModel.BookingDataBuilder.getbookingData;
import static DataModel.BookingDataBuilder.getpartialBookingData;
import static DataModel.TokenBuilder.getToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * This class contains end-to-end tests for the Restful Booker API.
 * It covers various scenarios such as creating a booking, retrieving a booking,
 * updating a booking, partially updating a booking, deleting a booking, and checking
 * if a booking is deleted.
 */
public class RestfulBookerE2ETests extends BaseTest {
    private static final Faker FAKER = Faker.instance();
    private BookingData newBooking;
    private BookingData updatedBooking;
    private PartialBookingData partialUpdateBooking;
    private TokenCreds tokenCreds;
    private int bookingId;

    /**
     * Method to set up test data before running the tests.
     */
    @BeforeTest
    public void testSetup() {
        newBooking = getbookingData();
        updatedBooking = getbookingData();
        partialUpdateBooking = getpartialBookingData();
        tokenCreds = getToken();
    }

    /**
     * Method to generate an authentication token.
     *
     * @return The generated authentication token.
     */
    private String generateToken() {
        return given().body(tokenCreds)
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .and()
                .assertThat()
                .body("token", Matchers.not(nullValue()))
                .extract()
                .path("token");
    }

    /**
     * Test method to create a new booking.
     */
    @Test
    public void createBookingTest() {
        bookingId = given().body(newBooking)
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .and()
                .assertThat()
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo(newBooking.getFirstname()))
                .body("booking.lastname", equalTo(newBooking.getLastname()))
                .body("booking.totalprice", equalTo(newBooking.getTotalprice()))
                .body("booking.depositpaid", equalTo(newBooking.isDepositpaid()))
                .body("booking.bookingdates.checkin", equalTo(newBooking.getBookingdates()
                        .getCheckin()))
                .body("booking.bookingdates.checkout", equalTo(newBooking.getBookingdates()
                        .getCheckout()))
                .body("booking.additionalneeds", equalTo(newBooking.getAdditionalneeds()))
                .extract()
                .path("bookingid");
    }

    /**
     * Test method to retrieve an existing booking.
     */
    @Test
    public void getBookingTest() {
        given().get("/booking/" + bookingId)
                .then()
                .statusCode(200)
                .and()
                .assertThat()
                .body("firstname", equalTo(newBooking.getFirstname()))
                .body("lastname", equalTo(newBooking.getLastname()))
                .body("totalprice", equalTo(newBooking.getTotalprice()))
                .body("depositpaid", equalTo(newBooking.isDepositpaid()))
                .body("bookingdates.checkin", equalTo(newBooking.getBookingdates()
                        .getCheckin()))
                .body("bookingdates.checkout", equalTo(newBooking.getBookingdates()
                        .getCheckout()))
                .body("additionalneeds", equalTo(newBooking.getAdditionalneeds()));
    }

    /**
     * Test method to update an existing booking.
     */
    @Test
    public void updateBookingTest() {
        given()
                .body(updatedBooking)
                .when()
                .header("Cookie", "token=" + generateToken())
                .put("/booking/" + bookingId)
                .then()
                .statusCode(200)
                .and()
                .assertThat()
                .body("firstname", equalTo(updatedBooking.getFirstname()))
                .body("lastname", equalTo(updatedBooking.getLastname()))
                .body("totalprice", equalTo(updatedBooking.getTotalprice()))
                .body("depositpaid", equalTo(updatedBooking.isDepositpaid()))
                .body("bookingdates.checkin", equalTo(updatedBooking.getBookingdates()
                        .getCheckin()))
                .body("bookingdates.checkout", equalTo(updatedBooking.getBookingdates()
                        .getCheckout()))
                .body("additionalneeds", equalTo(updatedBooking.getAdditionalneeds()));
    }

    /**
     * Test method to partially update an existing booking.
     */
    @Test
    public void updatePartialBookingTest() {
        given().body(partialUpdateBooking)
                .when()
                .header("Cookie", "token=" + generateToken())
                .patch("/booking/" + bookingId)
                .then()
                .statusCode(200)
                .and()
                .assertThat()
                .body("firstname", equalTo(partialUpdateBooking.getFirstname()))
                .body("lastname", equalTo(updatedBooking.getLastname()))
                .body("totalprice", equalTo(partialUpdateBooking.getTotalprice()))
                .body("depositpaid", equalTo(updatedBooking.isDepositpaid()))
                .body("bookingdates.checkin", equalTo(updatedBooking.getBookingdates()
                        .getCheckin()))
                .body("bookingdates.checkout", equalTo(updatedBooking.getBookingdates()
                        .getCheckout()))
                .body("additionalneeds", equalTo(updatedBooking.getAdditionalneeds()));
    }

    /**
     * Test method to delete an existing booking.
     */
    @Test(priority = 5)
    public void deleteBookingTest() {
        given().header("Cookie", "token=" + generateToken())
                .when()
                .delete("/booking/" + bookingId)
                .then()
                .statusCode(201);
    }

    /**
     * Test method to check if a booking is deleted.
     */
    @Test(priority = 6)
    public void checkBookingIsDeleted() {
        given().get("/booking/" + bookingId)
                .then()
                .statusCode(404);
    }

    @Test(dependsOnMethods = {"deleteBookingTest"})
    public void testDeleteBookingWhenIDDoesNotExist() {
        given().header("Cookie", "token=" + generateToken())
                .when()
                .delete("/booking/" + bookingId)
                .then()
                .statusCode(405);
    }

    @Test
    public void testBookingDetailsWithInvalidID() {
        float fakeBookingId = FAKER.number().numberBetween(-1, -100);
        given().pathParam("bookingId", fakeBookingId)
                .when()
                .get("/booking/{bookingId}")
                .then()
                .statusCode(404);
    }

}
