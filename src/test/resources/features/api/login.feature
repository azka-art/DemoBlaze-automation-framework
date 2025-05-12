Feature: Demoblaze Login API

  @api @smoke
  Scenario: Login with credentials returns response
    Given I have user credentials
    When I send a login request to the API
    Then the API response status code should be 200
    And the API response should contain expected content

  @api @negative
  Scenario: Login with non-existent user
    Given I have non-existent user credentials  
    When I send a login request to the API
    Then the API response status code should be 200
    And the API response should contain error message
