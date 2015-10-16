Feature: Community

  Scenario: Show new community dialog, pre-fill

    Given verified user "John Doe" with email john@doe.com exists
    And lives in "Prague, Czech Republic"

    When the comms/create page is shown
    Then field name is shown with value "Prague"
    And field location is shown with value "Prague, Czech Republic"


  Scenario: New community dialog, name min length validation

    Given verified user "John Doe" with email john@doe.com exists
    And lives in "Prague, Czech Republic"

    When the comms/create page is shown
    And name input field value is ""
    And newcommform-submit is disabled


  Scenario: New community dialog, name max length validation

    Given verified user "John Doe" with email john@doe.com exists
    And lives in "Prague, Czech Republic"

    When the comms/create page is shown
    And name input field value is "abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdef"
    And newcommform-submit is disabled


  Scenario: Create new community

    Given verified user "John Doe" with email john@doe.com exists
    And lives in "Prague, Czech Republic"
    And the comms/create page is shown

    When id newcommform-submit is clicked
    Then commname is shown with value "Prague"
