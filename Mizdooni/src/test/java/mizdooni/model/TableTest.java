package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {
    private Table table;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        table = new Table(1, 1, 4);
        reservation = new Reservation(null, null, table, LocalDateTime.parse("2024-10-10T15:00:00"));
    }

    @Test
    void testAddReservation() {
        table.addReservation(reservation);
        assertEquals(1, table.getReservations().size());
        assertEquals(reservation, table.getReservations().get(0));
    }

    @ParameterizedTest
    @CsvSource({
        "2024-10-10T15:00:00, true",
        "2024-10-10T16:00:00, false",
        "2024-10-10T15:00:01, false",
        "2024-10-10T14:59:59, false"
    })
    void testIsReserved(String dateTimeStr, boolean expected) {
        table.addReservation(reservation);
        LocalDateTime datetimeToCheck = LocalDateTime.parse(dateTimeStr);

        boolean result = table.isReserved(datetimeToCheck);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-10-10T15:00:00, true",
        "2024-10-10T15:00:01, false",
        "2024-10-10T14:59:59, false"
    })
    void testIsReservedWithLargeList(String time, boolean expected) {
        for (int i = 0; i < 100; i++) {
            LocalDateTime reservationTime = LocalDateTime.parse("2024-10-10T15:00:00").plusMinutes(i);
            Reservation newReservation = new Reservation(null, null, table, reservationTime);
            table.addReservation(newReservation);
        }
        LocalDateTime timeToCheck = LocalDateTime.parse(time);
        boolean result = table.isReserved(timeToCheck);
        assertEquals(expected, result);
    }

    @Test
    void testIsReservedWithEmptyReservations() {
        table.getReservations().clear();
        assertTrue(table.getReservations().isEmpty());
        LocalDateTime datetimeToCheck = LocalDateTime.parse("2024-10-10T15:00:00");
        boolean result = table.isReserved(datetimeToCheck);
        assertFalse(result);
    }

    @Test
    void testGetReservationsInitiallyEmpty() {
        assertNotNull(table.getReservations());
        assertTrue(table.getReservations().isEmpty());
    }
}
