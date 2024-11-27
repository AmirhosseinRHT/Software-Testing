package mizdooni.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.RestaurantSearchFilter;
import mizdooni.model.User;
import mizdooni.response.PagedList;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
    @Test
    void when_pageParameterIsMissing_expect_badRequest() throws Exception {
        mockMvc.perform(get("/restaurants"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void when_invalidPageParameter_expect_badRequest() throws Exception {
        mockMvc.perform(get("/restaurants")
                .param("page", "errorPage"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void when_noRestaurantsMatchFilter_expect_emptyList() throws Exception {
        int page = 1;
        RestaurantSearchFilter filter = new RestaurantSearchFilter();
        filter.setType("error");
        PagedList<Restaurant> emptyResult = new PagedList<>(List.of(), page,  1);
        when(restaurantService.getRestaurants(eq(page), any(RestaurantSearchFilter.class)))
            .thenReturn(emptyResult);
        mockMvc.perform(get("/restaurants")
                .param("page", String.valueOf(page))
                .param("type", "error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurants listed"))
            .andExpect(jsonPath("$.data.pageList").isEmpty())
            .andExpect(jsonPath("$.data.size").value(0))
            .andExpect(jsonPath("$.data.totalPages").value(0))
            .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void when_validPageAndFilterProvided_expect_restaurantsReturned() throws Exception {
        int page = 1;
        RestaurantSearchFilter filter = new RestaurantSearchFilter();
        filter.setName("Kababi");
        filter.setType("Iranian");
        filter.setLocation("Tehran");
        filter.setSort("rating");
        filter.setOrder("desc");
        PagedList<Restaurant> pagedRestaurants = new PagedList<>(
            List.of(restaurant1 , restaurant2), page, 2);
        when(restaurantService.getRestaurants(eq(page), any(RestaurantSearchFilter.class)))
            .thenReturn(pagedRestaurants);

        mockMvc.perform(get("/restaurants")
                .param("page", String.valueOf(page))
                .param("name", "Kababi")
                .param("type", "Iranian")
                .param("location", "Tehran")
                .param("sort", "rating")
                .param("order", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurants listed"))
            .andExpect(jsonPath("$.data.pageList[0].name").value("Kababi"))
            .andExpect(jsonPath("$.data.pageList[1].name").value("FastFood"))
            .andExpect(jsonPath("$.data.page").value(page))
            .andExpect(jsonPath("$.data.size").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    void when_getRestaurantsThrowsException_expect_BadRequestReturned() throws Exception {
        int page = 1;
        RestaurantSearchFilter filter = new RestaurantSearchFilter();
        filter.setName("Kababi");
        filter.setType("Iranian");
        filter.setLocation("Tehran");
        filter.setSort("rating");
        filter.setOrder("desc");
        when(restaurantService.getRestaurants(eq(page), any(RestaurantSearchFilter.class)))
            .thenThrow(new IllegalArgumentException("err"));

        mockMvc.perform(get("/restaurants")
                .param("page", String.valueOf(page))
                .param("name", "Kababi")
                .param("type", "Iranian")
                .param("location", "Tehran")
                .param("sort", "rating")
                .param("order", "desc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("err"));
    }


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

    @Test
    void when_validParameters_expect_restaurantAddedSuccessfully() throws Exception {
        Map<String, Object> validParams = Map.of(
            "name", "Kababi",
            "type", "Iranian",
            "startTime", "09:00",
            "endTime", "22:00",
            "description", "Iranian Restaurant",
            "image", "kababi.jpg",
            "address", Map.of(
                "country", "Iran",
                "city", "Tehran",
                "street", "Ferdowsi St."
            )
        );
        int mockRestaurantId = 1;
        when(restaurantService.addRestaurant(anyString(), anyString(), any(LocalTime.class), any(LocalTime.class), anyString(), any(
            Address.class), anyString()))
            .thenReturn(mockRestaurantId);
        mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(validParams)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("restaurant added"))
            .andExpect(jsonPath("$.data").value(mockRestaurantId));
    }

    @Test
    void when_missingRequiredParameters_expect_badRequest() throws Exception {
        Map<String, Object> invalidParams = Map.of(
            "name", "Kababi",
            "type", "Iranian"
        );

        mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidParams)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("parameters missing"));
    }

    @Test
    void when_invalidTimeFormat_expect_badRequest() throws Exception {
        Map<String, Object> invalidTimeParams = Map.of(
            "name", "Kababi",
            "type", "Iranian",
            "startTime", "invalidTime",
            "endTime", "22:00",
            "description", "Traditional Iranian Restaurant",
            "image", "kababi.jpg",
            "address", Map.of(
                "country", "Iran",
                "city", "Tehran",
                "street", "Ferdowsi St."
            )
        );

        mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidTimeParams)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("bad parameter type"));
    }

    @Test
    void when_invalidAddressFormat_expect_badRequest() throws Exception {
        Map<String, Object> invalidAddressParams = Map.of(
            "name", "Kababi",
            "type", "Iranian",
            "startTime", "09:00",
            "endTime", "22:00",
            "description", "Traditional Iranian Restaurant",
            "image", "kababi.jpg",
            "address", "invalidAddress"
        );

        mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAddressParams)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("bad parameter type"));
    }

    @Test
    void when_serviceThrowsException_expect_badRequest() throws Exception {
        Map<String, Object> validParams = Map.of(
            "name", "Kababi",
            "type", "Iranian",
            "startTime", "09:00",
            "endTime", "22:00",
            "description", "Traditional Iranian Restaurant",
            "image", "kababi.jpg",
            "address", Map.of(
                "country", "Iran",
                "city", "Tehran",
                "street", "Ferdowsi St."
            )
        );

        when(restaurantService.addRestaurant(anyString(), anyString(), any(LocalTime.class), any(LocalTime.class), anyString(), any(Address.class), anyString()))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(validParams)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void when_parametersAreNullOrEmpty_expect_badRequest() throws Exception {
        Map<String, Object> emptyParams = Map.of(
            "name", "",
            "type", "",
            "startTime", "09:00",
            "endTime", "22:00",
            "description", "",
            "address", Map.of(
                "country", "",
                "city", "",
                "street", ""
            )
        );

        mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(emptyParams)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("parameters missing"));
    }


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
