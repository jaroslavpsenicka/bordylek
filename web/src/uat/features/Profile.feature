Feature: Profile

  Scenario: Profile of known user

    Given verified user "John Doe" with email john@doe.com exists
    And living in "Prague, Czech Republic"
    When the profile page is shown
    Then username is shown with value "John Doe"
    And profilename is shown with value "John Doe"
    And location is shown with value "Prague, Czech Republic"


  Scenario: Profile of known user, member of one community

    Given community "Prague" exists
    And verified user "John Doe" with email john@doe.com exists
    And living in "Prague, Czech Republic"
    And member of community "Prague"

    When the profile page is shown
    Then xpath //li[contains(@class, 'profile-community')]/a is shown with value "Prague"
    And xpath //li[contains(@class, 'available-community')]/a is not shown


  Scenario: Profile of known user, navigate to community

    Given community "Prague" exists
    And verified user "John Doe" with email john@doe.com exists
    And living in "Prague, Czech Republic"
    And member of community "Prague"

    When the profile page is shown
    And xpath //li[contains(@class, 'profile-community')]/a is clicked
    Then xpath //h2[contains(@class, 'comm-header')] is shown with value "Prague"


  Scenario: Profile of known user, no community, one nearby

    Given community "Prague" exists
    And verified user "John Doe" with email john@doe.com exists
    And living in "Prague, Czech Republic"

    When the profile page is shown
    Then xpath //li[contains(@class, 'available-community')]/a is shown with value "Prague"
