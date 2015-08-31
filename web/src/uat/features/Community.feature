Feature: Community

  Scenario: Create new

    Given verified user "John Doe" with email john@doe.com exists
    And lives in "Prague, Czech Republic"

    When the comms/create page is shown
    # Then name is shown with value "Prague"
    # And location is shown with value "Prague, Czech Republic"


