Feature: Login

  Scenario: Successful login as john@doe.com

    Given the user "John Doe" with email john@doe.com exists
    When the index page is shown
    Then username is shown with value "John Doe"


  Scenario: Unknown user

    When the index page is shown
    Then username is not shown





