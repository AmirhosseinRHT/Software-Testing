package mizdooni.controllers;


import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    private int restaurantId;
    private String testComment;
    @InjectMocks
    private ReviewController reviewController;

    @Mock
    RestaurantService restaurantService;

    @Mock
    ReviewService reviewService;

    private Restaurant restaurant;

    private User user;
    private User user1;
    private User user2;
    private Rating rating1;
    private Rating rating2;

    @BeforeEach
    void setUp(){
        user = new User("test", "test123", "test@gmail.com", null, User.Role.manager);
        user1 = new User("test1", "test123", "test1@gmail.com", null, User.Role.client);
        user2 = new User("test2", "test123", "test2@gmail.com", null, User.Role.client);
        rating1 = makeRating(3.0, 4.0, 2.0, 4.5);
        rating2 = makeRating(3.5, 4.0, 3.0, 4.6);
        makeRestaurant();
        testComment = "test comment";
    }


    private Map<String, Object> makeRequestParamReview(Rating r, String comment){
        Map<String, Object> req = new HashMap<>();
        Map<String, Number> ratingMap = new HashMap<>();
        ratingMap.put("food", r.food);
        ratingMap.put("service", r.service);
        ratingMap.put("ambiance", r.ambiance);
        ratingMap.put("overall", r.overall);
        req.put("rating", ratingMap);
        req.put("comment", comment);
        return req;
    }

    private Rating makeRating(double food, double ambiance, double service, double overall) {
        Rating rating =new Rating();
        rating.food = food;
        rating.ambiance = ambiance;
        rating.service = service;
        rating.overall = overall;
        return rating;
    }

    private Restaurant makeRestaurant() {
        restaurant = new Restaurant("Test Restaurant", user, "Italian", LocalTime.of(10, 0),
                LocalTime.of(22, 0), "Roma", null, null);
        restaurant.addReview(new Review(user1, rating1, "test_comment", LocalDateTime.of(1402, 5, 13, 20, 5)));
        restaurant.addReview(new Review(user2, rating2, "test_comment", LocalDateTime.of(1402, 5, 13, 20, 5)));
        restaurantId = restaurant.getId();
        return restaurant;
    }
    private PagedList<Review> makeReviewPagedList(int page){
        return new PagedList<>(restaurant.getReviews(), page, 5);
    }

    @ParameterizedTest
    @CsvSource({
        "1, -1",
        "-1, 0",
        "1, -1",
    })
    public void testGetReviewsWithInvalidParameters(int restaurantId, int page) {
        ResponseException exception = assertThrows(ResponseException.class, () -> reviewController.getReviews(restaurantId,page));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    @DisplayName("Test get Review for a valid restaurant")
    public void testGetReviewsValidRestaurant() throws RestaurantNotFound {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(reviewService.getReviews(restaurantId, 1)).thenReturn(makeReviewPagedList(1));
        Response response = reviewController.getReviews(restaurantId, 1);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reviews for restaurant (" + restaurantId + "): " + restaurant.getName(),
                response.getMessage());
        PagedList<Review> resultData = (PagedList<Review>) response.getData();
        assertEquals(resultData.totalPages(), 1);
        assertEquals(resultData.getPageList().size(), 2);
        assertEquals(resultData.getPageList(), restaurant.getReviews());
    }

    @Test
    @DisplayName("Test get Review for an invalid restaurant")
    public void testGetReviewsInvalidRestaurant(){
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
        Response response = reviewController.getReviews(restaurantId, 1);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("restaurant not found", response.getMessage());
    }

    @Test
    @DisplayName("Test add Review for an valid restaurant")
    public void testAddReviewValidRestaurant()
            throws RestaurantNotFound, UserNotFound, ManagerCannotReview, UserHasNotReserved, InvalidReviewRating {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        doNothing().when(reviewService).addReview(restaurantId, rating1, testComment);
        Response response = reviewController.addReview(restaurantId, makeRequestParamReview(rating1, testComment));
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("review added successfully", response.getMessage());
    }

    @Test
    @DisplayName("Test add Review for an invalid restaurant")
    public void testAddReviewInvalidRestaurant() {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reviewController.addReview(restaurantId, makeRequestParamReview(rating1, testComment));
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "comment",
            "rating"
    })
    @DisplayName("Test add Review with an invalid parameter")
    public void testAddReviewWithInvalidParam(String key) {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        var reqParam = makeRequestParamReview(rating1, testComment);
        reqParam.remove(key);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reviewController.addReview(restaurantId, reqParam);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_MISSING, exception.getMessage());
    }

}
