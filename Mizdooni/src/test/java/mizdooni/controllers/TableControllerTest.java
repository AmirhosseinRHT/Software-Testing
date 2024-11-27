package mizdooni.controllers;

import java.time.LocalTime;
import java.util.Arrays;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.model.User;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private TableService tableService;

    Restaurant restaurant1 , restaurant2;
    Table table1 , table2 , table3;

    @BeforeEach
    public void setup(){
        User user = new User("test", "test123", "test@gmail.com", null, User.Role.manager);
        restaurant1 = new Restaurant("Kababi", user, "Iranian", LocalTime.of(0, 0),
            LocalTime.of(23, 59), "Tehran", null , "image");
        restaurant2 = new Restaurant("FastFood", user, "Italian", LocalTime.of(0, 0),
            LocalTime.of(23, 59), "Roma", null , "image");
        table1 = new Table(1, 1 , 7);
        table2 = new Table(2, 2 , 9);
        table3 = new Table(3, 2 , 10);
    }

    @Test
    void when_tablesListIsNotNull_expect_returnTablesList() throws Exception {
        int restaurantId = 1;
        List<Table> tables = Arrays.asList(table1 , table2);
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant1);
        when(tableService.getTables(restaurantId)).thenReturn(tables);
        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("tables listed"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(tables.size()));
    }

    @Test
    void when_tablesListIsNull_expect_ThrowNotFoundException() throws Exception {
        int restaurantId = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("restaurant not found"));
    }

    @Test
    void when_tablesListHasError_expect_ThrowException() throws Exception {
        int restaurantId = 2;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant2);
        when(tableService.getTables(restaurantId)).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
            .andExpect(status().isBadRequest())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("Error"));
    }

    @Test
    void when_restaurantIdIsWrong_expect_ThrowException() throws Exception {
        int restaurantId = -2;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant2);
        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
            .andExpect(status().isBadRequest())
            .andExpect(status().is4xxClientError());
    }

    @Test
    void when_addTableValidParams_expect_tableAdded() throws Exception {
        int restaurantId = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant1);
        String requestBody = """
                {
                    "seatsNumber": "4"
                }
                """;
        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("table added"));
    }

    @Test
    void when_addTableMissingParams_expect_throwBadRequestException() throws Exception {
        int restaurantId = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant1);
        String requestBody = "{}";
        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("parameters missing"));
    }

    @ParameterizedTest
    @CsvSource(value = {"-1", "1.1"})
    void when_addTableWithInvalidParams_expect_throwBadRequestException(String seatsNumber) throws Exception {
        int restaurantId = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant1);

        String requestBody = """
                {
                    "seatsNumber":\s""" + seatsNumber + """
                }
                """;

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("bad parameter type"));
    }

    @Test
    void when_addTableServiceThrowsError_expect_throwBadRequestException() throws Exception {
        int restaurantId = 2;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant2);
        doThrow(new RuntimeException("Error adding table"))
            .when(tableService).addTable(restaurantId, 4);

        String requestBody = """
                {
                    "seatNumber": "4"
                }
                """;

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("parameters missing"));
    }
}