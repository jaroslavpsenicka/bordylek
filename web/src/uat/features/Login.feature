Feature: Login

  Scenario: Successful login as john@doe.com, new user

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then welcome-header is shown with value "Welcome, John Doe It's Nice to Meet You!"
    And username is not shown
    And field name is shown with value "John Doe"


  Scenario: Successful login as john@doe.com, location popup

    Given new user "John Doe" with email john@doe.com exists
    When the index page is shown
    And location input field value is Prag

    Then xpath //li/a[contains(text(), 'Czech')] is shown with value "Prague, Czech Republic"


  Scenario: Successful login as john@doe.com, verified user

    Given verified user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then username is shown with value "John Doe"


  Scenario: Unknown user

    When the index page is shown
    Then username is not shown





