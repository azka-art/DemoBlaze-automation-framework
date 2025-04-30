Feature: Demoblaze Web Login

  @web @smoke
  Scenario: Successful login with valid credentials
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter valid username "testuser" and password "testpassword"
    And I click the login button
    Then I should be logged in successfully
    And I should see "Welcome testuser" message

  @web @negative
  Scenario: Failed login with invalid credentials
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter invalid username "invaliduser" and password "invalidpass"
    And I click the login button
    Then I should see an error message
