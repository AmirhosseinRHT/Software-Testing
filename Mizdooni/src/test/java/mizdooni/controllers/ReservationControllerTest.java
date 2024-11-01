package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.DATE_FORMATTER;
import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mizdooni.exceptions.BadPeopleNumber;
import mizdooni.exceptions.DateTimeInThePast;
import mizdooni.exceptions.InvalidManagerRestaurant;
import mizdooni.exceptions.InvalidWorkingTime;
import mizdooni.exceptions.ManagerReservationNotAllowed;
import mizdooni.exceptions.ReservationCannotBeCancelled;
import mizdooni.exceptions.ReservationNotFound;
import mizdooni.exceptions.ReservationNotInOpenTimes;
import mizdooni.exceptions.RestaurantNotFound;
import mizdooni.exceptions.TableNotFound;
import mizdooni.exceptions.UserNoAccess;
import mizdooni.exceptions.UserNotFound;
import mizdooni.exceptions.UserNotManager;
import mizdooni.model.Reservation;
import mizdooni.model.Restaurant;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private ReservationController reservationController;

    @Test
    @DisplayName("Test getReservations with restaurant not found")
    public void testRestaurantNotFound() {
        int restaurantId = 1;
        int table = 5;
        String validDate = "2023-12-12";
        ResponseException exception = assertThrows(ResponseException.class,
            () -> reservationController.getReservations(restaurantId, table, validDate));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test getReservations with invalid date format")
    public void testInvalidDateFormat() {
        int restaurantId = 1;
        int table = 5;
        String invalidDate = "2032-12-121";
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        ResponseException exception = assertThrows(ResponseException.class,
            () -> reservationController.getReservations(restaurantId, table, invalidDate));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    @DisplayName("Test getReservations with null date")
    public void testGetReservationWithNullDate() {
        int restaurantId = 1;
        int table = 5;
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, table, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    @DisplayName("Test getReservations with valid date and available reservations")
    public void testValidDateAndAvailableReservations()
        throws UserNotManager, TableNotFound, InvalidManagerRestaurant, RestaurantNotFound {
        int restaurantId = 1;
        int table = 5;
        String validDate = "2023-12-12";
        LocalDate localDate = LocalDate.parse(validDate, DATE_FORMATTER);
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        List<Reservation> reservations = Collections.singletonList(new Reservation(null,null, null, LocalDateTime.now()));
        when(reservationService.getReservations(restaurantId, table, localDate)).thenReturn(reservations);
        Response response = reservationController.getReservations(restaurantId, table, validDate);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("restaurant table reservations", response.getMessage());
        assertEquals(reservations, response.getData());
    }

    @Test
    @DisplayName("Test getReservations with Exception on getReservation")
    public void testExceptionOnGetReservation() {
        int restaurantId = 1;
        int table = 5;
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, table, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    @DisplayName("Test getCustomerReservations with existing reservations")
    public void testGetCustomerReservationsWithExistingReservations()
        throws UserNotFound, UserNoAccess {
        int customerId = 1;
        Reservation mockReservation = new Reservation(null, null, null, LocalDateTime.now());
        List<Reservation> mockReservations = List.of(mockReservation);
        when(reservationService.getCustomerReservations(customerId)).thenReturn(mockReservations);
        Response response = reservationController.getCustomerReservations(customerId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("user reservations", response.getMessage());
        assertEquals(mockReservations, response.getData());
    }

    @Test
    @DisplayName("Test getCustomerReservations with no reservations")
    public void testGetCustomerReservationsWithNoReservations() throws UserNotFound, UserNoAccess {
        int customerId = 2;
        when(reservationService.getCustomerReservations(customerId)).thenReturn(Collections.emptyList());
        Response response = reservationController.getCustomerReservations(customerId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("user reservations", response.getMessage());
        Assertions.assertTrue(((List<?>) response.getData()).isEmpty());
    }

    @Test
    @DisplayName("Test getCustomerReservations with service exception")
    public void testGetCustomerReservationsServiceException() throws UserNotFound, UserNoAccess {
        int customerId = 3;
        when(reservationService.getCustomerReservations(customerId))
            .thenThrow(new RuntimeException("Service failure"));
        var exception = assertThrows(ResponseException.class, () -> reservationController.getCustomerReservations(customerId));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Service failure", exception.getMessage());
    }

    @Test
    @DisplayName("Test cancelReservation with valid reservation number")
    public void testCancelReservationSuccessful()
        throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 123;
        doNothing().when(reservationService).cancelReservation(reservationNumber);
        Response response = reservationController.cancelReservation(reservationNumber);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reservation cancelled", response.getMessage());
        verify(reservationService).cancelReservation(reservationNumber);
    }

    @Test
    @DisplayName("Test cancelReservation with non-existing reservation number")
    public void testCancelReservationNotFound()
        throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 999;
        doThrow(new RuntimeException("Reservation not found"))
            .when(reservationService).cancelReservation(reservationNumber);
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.cancelReservation(reservationNumber));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Reservation not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test cancelReservation with unexpected exception")
    public void testCancelReservationUnexpectedException()
        throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 456;
        doThrow(new RuntimeException("Unexpected error"))
            .when(reservationService).cancelReservation(reservationNumber);
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.cancelReservation(reservationNumber));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    @DisplayName("Test getAvailableTimes with valid inputs")
    public void testGetAvailableTimesWithValidInputs()
        throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int restaurantId = 1;
        int people = 4;
        String date = "2024-11-01";
        LocalDate localDate = LocalDate.parse(date);
        List<LocalTime> availableTimes = List.of(LocalTime.of(12, 0), LocalTime.of(18, 0));

        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        when(reservationService.getAvailableTimes(restaurantId, people, localDate)).thenReturn(availableTimes);

        Response response = reservationController.getAvailableTimes(restaurantId, people, date);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("available times", response.getMessage());
        assertEquals(availableTimes, response.getData());
    }

    @Test
    @DisplayName("Test getAvailableTimes with invalid date format")
    public void testGetAvailableTimesWithInvalidDateFormat() {
        int restaurantId = 1;
        int people = 4;
        String date = "1999-23-24";
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getAvailableTimes(restaurantId, people, date));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    @DisplayName("Test getAvailableTimes with restaurant not found")
    public void testGetAvailableTimesRestaurantNotFound() {
        int restaurantId = 999;
        int people = 4;
        String date = "2024-11-01";
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getAvailableTimes(restaurantId, people, date));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test getAvailableTimes with unexpected exception from reserveService")
    public void testGetAvailableTimesWithUnexpectedException()
        throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int restaurantId = 1;
        int people = 4;
        String date = "2024-11-01";
        LocalDate localDate = LocalDate.parse(date);

        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        doThrow(new RuntimeException("Unexpected error"))
            .when(reservationService).getAvailableTimes(restaurantId, people, localDate);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getAvailableTimes(restaurantId, people, date));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    @DisplayName("Test addReservation with valid inputs")
    public void testAddReservationWithValidInputs()
        throws UserNotFound, DateTimeInThePast, TableNotFound,
        ReservationNotInOpenTimes, ManagerReservationNotAllowed,
        RestaurantNotFound, InvalidWorkingTime {
        int restaurantId = 1;
        Map<String, String> params = Map.of(
            "people", "4",
            "datetime", "2024-11-01 18:30"
        );

        LocalDateTime datetime = LocalDateTime.parse("2024-11-01T18:30:00");
        Reservation mockReservation = new Reservation(null, null, null, datetime);
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        when(reservationService.reserveTable(restaurantId, 4, datetime)).thenReturn(mockReservation);

        Response response = reservationController.addReservation(restaurantId, params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reservation done", response.getMessage());
        assertEquals(mockReservation, response.getData());
    }

    @Test
    @DisplayName("Test addReservation with missing parameters")
    public void testAddReservationWithMissingParameters() {
        int restaurantId = 1;
        Map<String, String> params = Map.of(
            "people", "4"
        );
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("parameters missing", exception.getMessage());
    }

    @Test
    @DisplayName("Test addReservation with invalid people type")
    public void testAddReservationWithInvalidPeopleType() {
        int restaurantId = 1;
        Map<String, String> params = Map.of(
            "people", "string",
            "datetime", "2024-11-01 18:30"
        );

        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    @DisplayName("Test addReservation with invalid datetime format")
    public void testAddReservationWithInvalidDatetimeFormat() {
        int restaurantId = 1;
        Map<String, String> params = Map.of(
            "people", "4",
            "datetime", "2024-11-01 18:30:00"
        );

        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    @DisplayName("Test addReservation with restaurant not found")
    public void testAddReservationRestaurantNotFound() {
        int restaurantId = 999;
        Map<String, String> params = Map.of(
            "people", "4",
            "datetime", "2024-11-01 18:30"
        );
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test addReservation with unexpected exception from reserveService")
    public void testAddReservationWithUnexpectedException()
        throws UserNotFound, DateTimeInThePast, TableNotFound,
        ReservationNotInOpenTimes, ManagerReservationNotAllowed,
        RestaurantNotFound, InvalidWorkingTime {
        int restaurantId = 1;
        Map<String, String> params = Map.of(
            "people", "4",
            "datetime", "2024-11-01 18:30"
        );

        LocalDateTime datetime = LocalDateTime.parse("2024-11-01T18:30:00");

        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));
        doThrow(new RuntimeException("Unexpected error"))
            .when(reservationService).reserveTable(restaurantId, 4, datetime);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    @DisplayName("Test addReservation with invalid of people")
    public void testAddReservationWithInvalidPeople() {
        int restaurantId = 1;
        Map<String, String> params = Map.of(
            "people", "-2",
            "datetime", "2024-11-01 18:30"
        );
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "1, 0",
        "1, -1",
        "1, -1.5",
    })
    public void testAddReservationInvalidParameters(int restaurantId, String peopleParam) {
        Map<String, String> params = new HashMap<>();
        params.put("people", peopleParam);
        params.put("datetime", LocalDateTime.now().toString());
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "-1, 0",
        "1, -1",
    })
    public void testGetReservationsWithInvalidParameters(int restaurantId, int people) {
        String validDate = "2023-12-12";
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, people , validDate));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }


    @ParameterizedTest
    @CsvSource({
        "0", "-3"
    })
    public void testGetCustomerReservationsWithInvalidParameters(int customerId) {
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getCustomerReservations(customerId));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "-1, 0",
        "1, -1",
    })
    public void testGetAvailableTimesWithInvalidParameters(int restaurantId, int people) {
        String validDate = "2023-12-12";
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant("test Restaurant"
            , null , null , null , null
            , null , null , null ));

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, people , validDate));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
        "0", "-3"
    })
    public void testCancelReservationsWithInvalidParameters(int reservationNumber) {
        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.cancelReservation(reservationNumber));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
