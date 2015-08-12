Feature: Login

  Scenario: Successful login via google.com

    Given the login page is shown
    And login-google is shown with value containing Google

    When login-google is clicked
    And submit_approve_access is clicked





