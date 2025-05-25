Feature: Demoblaze Web Login

  @web @smoke
  Scenario: Successful login with valid credentials
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter username "checkout_stable_user" and password "Test123"
    And I click the login button
    Then I should be logged in successfully
    And I should see "Welcome demotest_20250515_143128" message

  @web @negative
  Scenario: Login with invalid password
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter username "testuser2025" and password "wrongpassword"
    And I click the login button
    Then I should see error message "Wrong password."

  @web @negative
  Scenario: Login with non-existent user
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter username "nonexistentuser" and password "password123"
    And I click the login button
    Then I should see error message "User does not exist."

  @web @negative
  Scenario: Login with empty credentials
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I click the login button without entering credentials
    Then I should see error message "Please fill out Username and Password."

  @web @boundary
  Scenario: Login with special characters
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter username "user@#$%" and password "pass!@#"
    And I click the login button
    Then login should be processed with appropriate response