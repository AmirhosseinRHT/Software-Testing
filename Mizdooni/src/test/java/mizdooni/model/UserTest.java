package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Address address;
    private Restaurant italianRestaurant;
    private Restaurant iranianRestaurant;
    private Table table1;
    private Table table2;
    

    @BeforeEach
    void setUp() {
        address = new Address("Iran" , "Tehran" , "Kargar St");
        User manager = new User("user", "pass123", "user@gmail.com", address, User.Role.manager);
        italianRestaurant = new Restaurant("Italian Restaurant", manager, "Italian", LocalTime.of(10, 0), LocalTime.of(22, 0),
            "Milan", address, "good image");
        iranianRestaurant = new Restaurant("Iranian Restaurant", manager, "Iranian", LocalTime.of(10, 0), LocalTime.of(22, 0),
            "Isfahan", address, "good image");
        table1 = new Table(1,1,4);
        table2 = new Table(2,2,2);
        
        italianRestaurant.addTable(table1);
        iranianRestaurant.addTable(table1);
        user = new User("Amirhossein", "test123", "amir@gmail.com", address, User.Role.client);
    }

    @Test
    void testAddReservation() {
        Reservation reservation = new Reservation(user,
            italianRestaurant, table1, LocalDateTime.now().plusDays(1));
        user.addReservation(reservation);

        assertEquals(1, user.getReservations().size());
        assertEquals(reservation, user.getReservations().get(0));
        assertEquals(0, reservation.getReservationNumber());
    }

    @Test
    void testAddMultipleReservations() {
        Reservation reservation1 = new Reservation(user,
            italianRestaurant, table1, LocalDateTime.now().plusDays(1));
        Reservation reservation2 = new Reservation(user,
            italianRestaurant, table1, LocalDateTime.now().plusDays(2));

        user.addReservation(reservation1);
        user.addReservation(reservation2);

        assertEquals(2, user.getReservations().size());
        assertEquals(0, reservation1.getReservationNumber());
        assertEquals(1, reservation2.getReservationNumber());
    }

    @ParameterizedTest
    @CsvSource({
        "2023-10-10T10:00:00, false",
        "2023-10-10T15:00:00, true",
        "2023-10-10T23:00:00, false",
        "2023-10-10T08:00:00, false",
        "2025-10-10T10:00:00, false",
    })
    void testCheckReservationTimeIsValid(LocalDateTime dateTime, boolean expected) {
        Reservation reservation = new Reservation(user, italianRestaurant, table1, dateTime);
        user.addReservation(reservation);
        boolean result = user.checkReserved(italianRestaurant);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "italianRestaurant, true",
        "iranianRestaurant, false"
    })
    void testCheckRestaurantValidity(String restaurantName, boolean expected) {
        LocalDateTime time = LocalDateTime.parse("2023-10-10T15:00:00");
        Reservation reservation = new Reservation(user, italianRestaurant, table1, time);
        user.addReservation(reservation);

        Restaurant restaurantToCheck = restaurantName.equals("italianRestaurant") ? italianRestaurant : iranianRestaurant;
        boolean result = user.checkReserved(restaurantToCheck);

        assertEquals(expected, result);
    }

    @Test
    void testCheckReservedNoReservation() {
        boolean result = user.checkReserved(italianRestaurant);
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({
        "0, false, true",
        "2, false, false",
        "1, true, false"
    })
    void testGetReservation(int reservationNumber, boolean isCancelled, boolean expected) {
        Reservation reservation = new Reservation(user,
            italianRestaurant, table1, LocalDateTime.now().minusDays(1));
        reservation.setReservationNumber(1);
        if (isCancelled)
            reservation.cancel();
        user.addReservation(reservation);
        Reservation result = user.getReservation(reservationNumber);
        if (expected) {
            assertNotNull(result);
            assertEquals(reservation, result);
        } else {
            assertNull(result);
        }
    }

    @Test
    void testGetReservationNoReservations() {
        Reservation result = user.getReservation(1);
        assertNull(result);
    }

    @Test
    void testGetMultipleReservations() {
        Reservation reservation1 = new Reservation(user,
            italianRestaurant, table1, LocalDateTime.now().minusDays(1));
        Reservation reservation2 = new Reservation(user,
            italianRestaurant, table1, LocalDateTime.now().plusDays(1));
        reservation1.setReservationNumber(1);
        reservation2.setReservationNumber(2);
        user.addReservation(reservation1);
        user.addReservation(reservation2);
        Reservation result1 = user.getReservation(0);
        Reservation result2 = user.getReservation(1);
        assertEquals(reservation1, result1);
        assertEquals(reservation2, result2);
    }


    @ParameterizedTest
    @CsvSource(value = {
        "test123, true",
        "test12, false",
    })
    void testCheckPassword(String inputPassword, boolean expected) {
        boolean result = user.checkPassword(inputPassword);
        assertEquals(expected, result);
    }

    @Test
    void testCheckPasswordEmptyPassword() {
        boolean result = user.checkPassword("");
        assertFalse(result);
    }

    @Test
    void testCheckPasswordNullPassword() {
        //This test fails . Because of unhandled null argument.
        assertThrows(NullPointerException.class, () -> user.checkPassword(null));
    }
}
