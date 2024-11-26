package mizdooni.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mizdooni.model.Restaurant;
import mizdooni.model.User;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
public class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    Restaurant restaurant1, restaurant2;

    @BeforeEach
    public void setup() {
        User user = new User("test", "test123", "test@gmail.com", null, User.Role.manager);
        restaurant1 = new Restaurant("Kababi", user, "Iranian", LocalTime.of(0, 0),
            LocalTime.of(23, 59), "Tehran", null, "image");
        restaurant2 = new Restaurant("FastFood", user, "Italian", LocalTime.of(0, 0),
            LocalTime.of(23, 59), "Roma", null, "image");
    }

    @Test
    void when_getRestaurantIdISWrong_expect_ReturnInvalidId() throws Exception {
        int restaurantId = -1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("invalid restaurant id"));
    }

    @Test
    void when_getRestaurantByIdExists_expect_restaurantFound() throws Exception {
        int restaurantId = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant1);
        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurant found"))
            .andExpect(jsonPath("$.data.name").value(restaurant1.getName()));
    }

    @Test
    void when_getRestaurantByIdNotExists_expect_throwNotFoundException() throws Exception {
        int restaurantId = 20;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("restaurant not found"));
    }

    //////////////////////
    // getRestaurants(@RequestParam int page, RestaurantSearchFilter filter)
    /////////////////////

    @Test
    void when_validManagerId_expect_restaurantsListed() throws Exception {
        int managerId = 1;
        List<Restaurant> mockRestaurants = List.of(restaurant1, restaurant2);
        when(restaurantService.getManagerRestaurants(managerId)).thenReturn(mockRestaurants);
        mockMvc.perform(get("/restaurants/manager/{managerId}", managerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("manager restaurants listed"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(mockRestaurants.size()))
            .andExpect(jsonPath("$.data[0].name").value(restaurant1.getName()))
            .andExpect(jsonPath("$.data[1].name").value(restaurant2.getName()));
    }

    @Test
    void when_invalidManagerId_expect_badRequest() throws Exception {
        int managerId = 0;
        mockMvc.perform(get("/restaurants/manager/{managerId}", managerId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("invalid manager id"));
    }

    @Test
    void when_serviceGetManagerRestaurantsThrowsException_expect_badRequest() throws Exception {
        int managerId = 1;
        when(restaurantService.getManagerRestaurants(managerId)).thenThrow(new RuntimeException("Unexpected error"));
        mockMvc.perform(get("/restaurants/manager/{managerId}", managerId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    ///////////////////////
    // addRestaurant(@RequestBody Map<String, Object> params)
    ///////////////////////

    @Test
    void when_restaurantNameIsAvailable_expect_successMessage() throws Exception {
        String restaurantName = "Res";
        when(restaurantService.restaurantExists(restaurantName)).thenReturn(false);
        mockMvc.perform(get("/validate/restaurant-name")
                .param("data", restaurantName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurant name is available"));
    }

    @Test
    void when_restaurantNameIsTaken_expect_conflictError() throws Exception {
        String restaurantName = "Res";
        when(restaurantService.restaurantExists(restaurantName)).thenReturn(true);
        mockMvc.perform(get("/validate/restaurant-name")
                .param("data", restaurantName))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("restaurant name is taken"));
    }

    @Test
    void when_RestaurantExistMethodThrowsException_expect_badRequest() throws Exception {
        String restaurantName = "err";
        when(restaurantService.restaurantExists(restaurantName)).thenThrow(new RuntimeException("Unexpected error"));
        mockMvc.perform(get("/validate/restaurant-name")
                .param("data", restaurantName))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void when_restaurantTypesAreReturned_expect_successMessage() throws Exception {
        Set<String> restaurantTypes = Set.of("Italian", "Chinese", "Mexican");
        when(restaurantService.getRestaurantTypes()).thenReturn(restaurantTypes);
        mockMvc.perform(get("/restaurants/types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurant types"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(restaurantTypes.size()));
    }


    @Test
    void when_getRestaurantTypesThrowsException_expect_badRequest() throws Exception {
        when(restaurantService.getRestaurantTypes()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/restaurants/types"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void when_restaurantLocationsAreReturned_expect_successMessage() throws Exception {
        Map<String, Set<String>> locations = Map.of(
            "USA", Set.of("New York", "LA"),
            "Italy", Set.of("Rome", "Milan")
        );
        when(restaurantService.getRestaurantLocations()).thenReturn(locations);
        mockMvc.perform(get("/restaurants/locations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurant locations"))
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.USA").isArray())
            .andExpect(jsonPath("$.data.Italy").isArray());
    }

    @Test
    void when_getRestaurantLocationsThrowsException_expect_badRequest() throws Exception {
        when(restaurantService.getRestaurantLocations()).thenThrow(new RuntimeException("Unexpected error"));
        mockMvc.perform(get("/restaurants/locations"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

}
