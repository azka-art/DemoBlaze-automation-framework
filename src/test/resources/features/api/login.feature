Feature: Demoblaze Login API

  Background:
    Given the API base URL is configured

  @api @smoke
  Scenario: Successful login with valid credentials
    Given I have valid user credentials
    When I send a login request to the API
    Then the API response status code should be 200
    And the API response should contain auth token

  @api @negative
  Scenario: Login with invalid password
    Given I have user credentials with wrong password
    When I send a login request to the API
    Then the API response status code should be 200
    And the API response should contain error message "Wrong password"

  @api @negative
  Scenario: Login with non-existent user
    Given I have non-existent user credentials  
    When I send a login request to the API
    Then the API response status code should be 200
    And the API response should contain error message "User does not exist"

  @api @negative
  Scenario: Login with empty credentials
    Given I have empty user credentials
    When I send a login request to the API
    Then the API response status code should be 500
    And the API response should contain error message

  @api @boundary
  Scenario: Login with special characters in username
    Given I have user credentials with special characters
    When I send a login request to the API
    Then the API response status code should be 200