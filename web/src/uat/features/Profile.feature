Feature: Profile

  Scenario: Profile of known user, no photo

    Given verified user "John Doe" with email john@doe.com exists
    And living in "Prague, Czech Republic"
    When the profile page is shown
    Then username is shown with value "John Doe"
    And profilename is shown with value "John Doe"
    And location is shown with value "Prague, Czech Republic"






