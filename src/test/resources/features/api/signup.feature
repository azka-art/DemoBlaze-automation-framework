@api
Feature: Demoblaze Signup API

  Background:
    Given the API base URL is configured

  @api @smoke
  Scenario: Successful signup with new user
    Given I have new user signup credentials
    When I send a signup request to the API
    Then the API response status code should be 200
    And the API response should contain success message

  @api @negative
  Scenario: Signup with existing username
    Given I have existing user credentials for signup
    When I send a signup request to the API
    Then the API response status code should be 200
    And the API response should contain error message "This user already exist."

  @api @negative
  Scenario: Signup with empty username
    Given I have signup credentials with empty username
    When I send a signup request to the API
    Then the API response status code should be 500
    And the API response should contain error message

  @api @negative
  Scenario: Signup with empty password
    Given I have signup credentials with empty password
    When I send a signup request to the API
    Then the API response status code should be 200
    And the API response should contain success message

  @api @boundary
  Scenario: Signup with very long username
    Given I have signup credentials with long username
    When I send a signup request to the API
    Then the API response status code should be 200