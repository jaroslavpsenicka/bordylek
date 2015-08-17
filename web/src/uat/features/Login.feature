Feature: Login

  Scenario: New user

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then welcome-header is shown with value "Welcome, John Doe It's Nice to Meet You!"
    And username is not shown
    And field name is shown with value "John Doe"


  Scenario: Location popup

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    And location input field value is Prag

    Then xpath //li/a[contains(text(), 'Czech')] is shown with value "Prague, Czech Republic"


  Scenario: Confirm button

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then welcomeform-submit is disabled
    When location input field value is Prague, Czech Republic
    Then welcomeform-submit is enabled

  Scenario: Successful login as john@doe.com, verified user

    Given verified user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then username is shown with value "John Doe"


  Scenario: Unknown user

    When the index page is shown
    Then username is not shown





