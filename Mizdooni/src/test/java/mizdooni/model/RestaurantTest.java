package mizdooni.model;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {

    private Restaurant restaurant;
    private User user;
    private Table table1;
    private Table table2;

    static Stream<Arguments> provideSeatsLists() {
        return Stream.of(
            Arguments.of(34 , List.of(1, 2, 34, 5, 6, 6, 7, 8, 8)),
            Arguments.of(12 , List.of(0, 0, 0, 12, 4, 8, 9)),
            Arguments.of(100 , List.of(15, 12, 12, 12, 17, 100)),
            Arguments.of(0 ,List.of(0, 0, 0, 0, 0, 0)),
            Arguments.of(1 ,List.of(1, 1, 1, 1, 1, 1))
        );
    }

    static Stream<Arguments> provideRatings() {
        return Stream.of(
            Arguments.of(3,new double[]{3.5, 4.0, 3.5, 3.8} ,new double[]{4.0, 5.0, 3.5}, new double[]{3.5, 4.5, 4.0}, new double[]{5.0, 4.0, 3.5}, new double[]{4.5, 4.5, 3.8}),
            Arguments.of(2,new double[]{3.5, 4.0, 3.0, 3.8}, new double[]{4.5, 3.5}, new double[]{4.0, 4.0}, new double[]{3.5, 3.0}, new double[]{4.2, 3.8}),
            Arguments.of(4,new double[]{3.0, 3.5, 3.0, 3.6}, new double[]{5.0, 3.5, 4.0, 3.0}, new double[]{4.5, 4.0, 4.0, 3.5}, new double[]{4.5, 3.5, 4.0, 3.0}, new double[]{4.5, 4.0, 4.2, 3.6}),
            Arguments.of(1,new double[]{4.0, 4.0, 3.0, 3.8}, new double[]{4.0}, new double[]{4.0}, new double[]{3.0}, new double[]{3.8}),
            Arguments.of(5,new double[]{3.5, 4.0, 3.0, 3.7}, new double[]{3.5, 4.0, 4.5, 5.0, 3.5}, new double[]{4.5, 4.0, 4.5, 3.5, 4.0}, new double[]{3.0, 3.5, 4.0, 4.5, 3.0}, new double[]{4.0, 3.9, 4.3, 4.7, 3.7}),
            Arguments.of(5,new double[]{0, 0, 0, 0}, new double[]{0, 0,0, 0, 0}, new double[]{0, 0, 0, 0, 0}, new double[]{0, 0, 0, 0, 0}, new double[]{0, 0, 0, 0, 0})
        );
    }

    static Stream<Arguments> providedTables() {
        return Stream.of(
            Arguments.of(List.of(), 0, null),
            Arguments.of(List.of(new Table(1, 0, 4)), 1, new Table(1, 0, 4)), // One table
            Arguments.of(List.of(new Table(1, 0, 4), new Table(2, 0, 2)), 2, new Table(2, 0, 2)), // Two tables
            Arguments.of(List.of(new Table(1, 0, 4), new Table(2, 0, 2), new Table(3, 0, 8)), 3, new Table(3, 0, 8)) // Three tables
        );
    }

    static Stream<Arguments> RatingValuesAndExpectedStars() {
        return Stream.of(
            Arguments.of(0.0, 0, "test"),
            Arguments.of(1.2, 1, "test"),
            Arguments.of(2.5, 3, "test"),
            Arguments.of(3.7, 4, "test"),
            Arguments.of(4.8, 5, "test"),
            Arguments.of(5.0, 5, "test")
        );
    }

    @BeforeEach
    void setUp() {
        user = new User("test", "test123", "test@gmail.com", null, User.Role.manager);
        restaurant = new Restaurant("Test Restaurant", user, "Italian", LocalTime.of(10, 0),
            LocalTime.of(22, 0), "Roma", null, null);
        table1 = new Table(1, restaurant.getId(), 4);
        table2 = new Table(2, restaurant.getId(), 2);
    }

    @ParameterizedTest
    @MethodSource("providedTables")
    void testAddTable(List<Table> tablesToAdd, int expectedSize, Table lastAddedTable) {
        tablesToAdd.forEach(restaurant::addTable);
        assertEquals(expectedSize, restaurant.getTables().size());
        if (lastAddedTable != null) {
            int tablesCount  = restaurant.getTables().size();
            Table lastTableInRestaurant = restaurant.getTables().get(tablesCount-1);
            assertEquals(lastAddedTable.getTableNumber(), lastTableInRestaurant.getTableNumber());
            assertEquals(lastAddedTable.getSeatsNumber(), lastTableInRestaurant.getSeatsNumber());
        }
    }

    @Test
    void testGetTable() {
        restaurant.addTable(table1);
        restaurant.addTable(table2);

        assertEquals(table1, restaurant.getTable(1));
        assertEquals(table2, restaurant.getTable(2));
        assertNull(restaurant.getTable(3));
    }

    @ParameterizedTest
    @MethodSource("provideSeatsLists")
    void testGetMaxSeatsNumber(Integer expected , List<Integer> tableSeats) {
        for (int i = 0; i < tableSeats.size(); i++) {
            Table t = new Table(i, 0, tableSeats.get(i));
            restaurant.addTable(t);
        }
        int result = restaurant.getMaxSeatsNumber();
        assertEquals(expected, result);
    }

    @Test
    void testAddReview() {
        LocalDateTime now = LocalDateTime.now();
        Rating rating = new Rating();
        rating.food = 5;
        rating.service = 5;
        rating.ambiance = 4;
        rating.overall = 4.5;
        Review review1 = new Review(user, rating, "Great food!", now);

        restaurant.addReview(review1);
        assertEquals(1, restaurant.getReviews().size());
        assertEquals(review1, restaurant.getReviews().get(0));
        Review review2 = new Review(user, new Rating(), "Excellent service!", now);
        restaurant.addReview(review2);

        assertEquals(1, restaurant.getReviews().size());
        assertEquals(review2, restaurant.getReviews().get(0));
    }

    @ParameterizedTest
    @MethodSource("provideRatings")
    void testGetAverageRating(int reviewCount, double[] expected, double[] foodRatings, double[] serviceRatings, double[] ambianceRatings, double[] overallRatings) {

        for (int i = 0; i < reviewCount; i++) {
            Rating rating = new Rating();
            rating.food = foodRatings[i];
            rating.service = serviceRatings[i];
            rating.ambiance = ambianceRatings[i];
            rating.overall = overallRatings[i];
            Review review = new Review(user, rating, "Review " + (i + 1), LocalDateTime.now());
            restaurant.addReview(review);
        }
        Rating averageRating = restaurant.getAverageRating();
        assertEquals(expected[0], averageRating.food);
        assertEquals(expected[1], averageRating.service);
        assertEquals(expected[2], averageRating.ambiance);
        assertEquals(expected[3], averageRating.overall);
    }

    @ParameterizedTest
    @MethodSource("RatingValuesAndExpectedStars")
    void testGetStarCount(double overallRating, int expectedStars, String comment) {
        LocalDateTime now = LocalDateTime.now();
        Rating rating = new Rating();
        rating.overall = overallRating;
        Review review = new Review(user, rating, comment, now);
        restaurant.addReview(review);
        int starCount = restaurant.getStarCount();
        assertEquals(expectedStars, starCount);
    }
}
