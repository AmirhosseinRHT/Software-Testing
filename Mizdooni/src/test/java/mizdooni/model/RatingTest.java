package mizdooni.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class RatingTest {

    @ParameterizedTest
    @CsvSource({
        "0.0, 0",
        "1.4, 1",
        "1.5, 2",
        "2.0, 2",
        "2.9, 3",
        "3.4, 3",
        "3.5, 4",
        "4.0, 4",
        "4.4999999 , 4",
        "4.5000001, 5",
        "4.6, 5",
        "5.0, 5",
        "5.5, 5",
        "6.0, 5"
    })
    void testGetStarCount(double overallRating, int expectedStarCount) {
        Rating rating = new Rating();
        rating.overall = overallRating;

        int result = rating.getStarCount();
        assertEquals(expectedStarCount, result);
    }
}
