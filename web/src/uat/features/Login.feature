Feature: Login

  Scenario: Successful login via google.com

    Given the login page is shown
    And login-google is shown with value containing Google

    When login-google is clicked
    And Email input field value is thelittlebighand@gmail.com
    And next is clicked
    And Passwd input field value is hand1234
    And signIn is clicked
    And submit_approve_access is clicked

    Then username is shown with value "Little BigHand"





