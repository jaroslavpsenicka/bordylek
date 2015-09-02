Feature: Community

  Scenario: Create new

    Given verified user "John Doe" with email john@doe.com exists
    And lives in "Prague, Czech Republic"

    When the comms/create page is shown
    Then field name is shown with value "John Doe"
    And field location is shown with value "Prague, Czech Republic"


