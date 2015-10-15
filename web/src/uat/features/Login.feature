Feature: Login

  Scenario: New user

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then welcome-header is shown with value "Welcome, John Doe"
    And username is not shown
    And field name is shown with value "John Doe"


  Scenario: Location popup

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    And location input field value is "Prag"

    Then xpath //li/a[contains(text(), 'Czech')] is shown with value "Prague, Czech Republic"


  Scenario: Confirm button enabling

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then welcomeform-submit is disabled
    When location input field value is "Prague"
    And ENTER key is pressed
    And welcomeform-submit is enabled


  Scenario: User confirmation

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    When location input field value is "Prague"
    And ENTER key is pressed
    And id welcomeform-submit is clicked
    And username is shown with value "John Doe"


  Scenario: User confirmation, native name

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    And location input field value is "Černošice"
    Then xpath //li/a[contains(text(), 'Czech')] is shown with value "Černošice, Czech Republic"
    And ENTER key is pressed
    And id welcomeform-submit is clicked
    And username is shown with value "John Doe"


  Scenario: User confirmation, illegal name

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    And location input field value is "QWERTYUIOP"
    And ENTER key is pressed
    Then welcomeform-submit is disabled


  Scenario: Successful login as john@doe.com, verified user

    Given verified user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then username is shown with value "John Doe"


  Scenario: Unknown user

    When the index page is shown
    Then username is not shown





