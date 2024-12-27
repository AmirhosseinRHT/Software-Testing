package mizdooni.model;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CucumberContextConfiguration
public class UserScenarioTest {

    private User user;
    private User user2;
    private Address address;
    private Restaurant italianRestaurant;
    private Restaurant iranianRestaurant;
    private Reservation reservation;
    private Restaurant restaurant;
    private boolean reservationAdded;
    private Table table1;
    private Table table2;
    private Review review;
    private boolean submitedReview = false;

    @Given("a user named {string} with {string} role")
    public void givenUserWithRole(String username, String role) {
        User.Role userRole = User.Role.valueOf(role.toLowerCase());
        user = new User(username, "password123", "email@example.com",
                new Address("temp", "temp", "temp"), userRole);
    }

    @Given("a restaurant {string} with available time slots")
    public void givenARestaurantWithAvailableTimeSlots(String restaurantName) {
        restaurant = new Restaurant(restaurantName, null, "Iranian", LocalTime.of(10, 0), LocalTime.of(22, 0),
                "Isfahan", address, "good image");
    }

    @Given("a reservation on {string} at {string}")
    public void givenRestaurantReservationForSpecificDateAndTime(String date, String time) {
        reservation = new Reservation(user, restaurant, table1, LocalDateTime.parse(date + "T" + time));
    }

    @When("I add the reservation for the user")
    public void whenIAddTheReservationForTheUser() {
        try {
            user.addReservation(reservation);
            reservationAdded = true;
        } catch (Exception e) {
            reservationAdded = false;
        }
    }

    @When("I try to add a null reservation")
    public void whenITryToAddANullReservation() {
        reservationAdded = false;
        try {
            user.addReservation(null);
            reservationAdded = true;
        } catch (Exception e) {
            reservationAdded = false;
        }
    }

    @Then("the reservation should be added successfully")
    public void thenTheReservationShouldBeAddedSuccessfully() {
        assertTrue(reservationAdded);
        assertEquals(reservation, user.getReservations().get(0));
        assertEquals(0, reservation.getReservationNumber());
    }

    @Then("the reservation should not be added")
    public void thenTheReservationShouldNotBeAdded() {
        assertFalse(reservationAdded);
        assertEquals(0, user.getReservations().size());
    }

    @When("a review with rating food={double} service={double} ambiance={double} overall={double}")
    public void reviewWithRatingFoodServiceAmbianceOverall(double food, double service, double ambiance, double overall) {
        Rating rating = new Rating();
        rating.food = food;
        rating.service = service;
        rating.ambiance = ambiance;
        rating.overall = overall;
        review = new Review(user, rating, "good", LocalDateTime.parse("2023-10-10T15:00:00"));
    }

    @When("submit review")
    public void submitReview() {
        submitedReview = false;
        try {
            restaurant.addReview(review);
            submitedReview = true;
        } catch (Exception e) {
            submitedReview = false;
        }
    }

    @When("submit invalid review")
    public void submitInvalidReview() {
        submitedReview = false;
        try {
            restaurant.addReview(null);
            submitedReview = true;
        } catch (Exception e) {
            submitedReview = false;
        }
    }

    @Then("the review should be submit successfully")
    public void theReviewShouldBeSubmitSuccessfully() {
        assertTrue(submitedReview);
        assertFalse(restaurant.getReviews().isEmpty());
        assertTrue(restaurant.getReviews().contains(review));
    }

    @Then("the review should not be submit successfully")
    public void theReviewShouldNotBeSubmitSuccessfully() {
        assertFalse(submitedReview);
    }

    @Then("rating overall at index {int} must be {double}")
    public void inGivenIndexRatingOverallMustBe(int index, double overall) {
        assertEquals(overall, restaurant.getReviews().get(index).getRating().overall);
    }

    @Then("the in average rating must have food={double} service={double} ambiance={double} overall={double}")
    public void theAverageValuesMustBe(double food, double service, double ambiance, double overall) {
        Rating average = restaurant.getAverageRating();
        assertEquals(food, average.food);
        assertEquals(service, average.service);
        assertEquals(ambiance, average.ambiance);
        assertEquals(overall, average.overall);

    }
}
