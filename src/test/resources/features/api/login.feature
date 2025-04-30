Feature: Demoblaze Login API

  @api @smoke
  Scenario: Successful login with valid credentials
    Given I have valid user credentials
    When I send a login request to the API
    Then the API response status code should be 200
    And the API response should contain auth token

  @api @negative
  Scenario: Failed login with invalid credentials
    Given I have invalid user credentials
    When I send a login request to the API
    Then the API response status code should be 400
    And the API response should contain error message
