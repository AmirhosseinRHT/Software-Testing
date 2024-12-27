Feature: Add

  Scenario: Successfully adding a reservation for a user
    Given a user named "Ali" with "client" role
    And a restaurant "Akbar joojeh" with available time slots
    And a reservation on "2024-12-25" at "18:00"
    When I add the reservation for the user
    Then the reservation should be added successfully

  Scenario: Adding a reservation when restaurant is unavailable at the chosen time
    Given a user named "Ahmad" with "client" role
    And a restaurant "Akbar jojeh" with available time slots
    And a reservation on "2024-12-25" at "22:00" for 3 people
    When I add the reservation for the user
    Then the reservation should not be added

  Scenario: Adding a reservation with invalid data (null reservation)
    Given a user named "Fatemeh" with "client" role
    When I try to add a null reservation
    Then the reservation should not be added