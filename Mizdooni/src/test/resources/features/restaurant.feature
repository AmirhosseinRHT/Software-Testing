Feature: Restaurant

  Scenario: Successfully submit review for a user
    Given a user named "Ali" with "client" role
    And a restaurant "Akbar joojeh" with available time slots
    And a reservation on "2024-12-25" at "18:00"
    And a review with rating food=5.5 service=3.0 ambiance=5.0 overall=4.2
    When submit review
    Then the review should be submit successfully

  Scenario: Successfully submit invalid review for a user
    Given a user named "Ali" with "client" role
    And a restaurant "Akbar joojeh" with available time slots
    When submit invalid review
    Then the review should not be submit successfully

  Scenario: Successfully replace review for a user
    Given a user named "Ali" with "client" role
    And a restaurant "Akbar joojeh" with available time slots
    And a reservation on "2024-12-25" at "18:00"
    And a review with rating food=5.5 service=3.0 ambiance=5.0 overall=4.2
    When submit review
    Then the review should be submit successfully
    Given a review with rating food=5.0 service=3.0 ambiance=5.0 overall=4.0
    When submit review
    Then the review should be submit successfully
    Then rating overall at index 0 must be 4.0

  Scenario: The average of ratings is correct
    Given a user named "Ali" with "client" role
    And a restaurant "Akbar joojeh" with available time slots
    And a review with rating food=5.4 service=3.0 ambiance=5.0 overall=4.2
    When submit review
    Then the review should be submit successfully
    Given a user named "Hasan" with "client" role
    And a review with rating food=3.0 service=4.0 ambiance=1.0 overall=2.0
    When submit review
    Then the review should be submit successfully
    Then the in average rating must have food=4.2 service=3.5 ambiance=3.0 overall=3.1

  Scenario: The average of empty ratings is correct
    Given a user named "Ali" with "client" role
    And a restaurant "Akbar joojeh" with available time slots
    Then the in average rating must have food=0.0 service=0.0 ambiance=0.0 overall=0.0