Feature: Demoblaze Web Login

  @web @smoke
  Scenario: Login form interaction
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter username "test" and password "test"
    And I click the login button
    Then I should see a login result

  @web @negative
  Scenario: Invalid login attempt
    Given I am on the Demoblaze homepage
    When I click on the login button in the navigation bar
    And I enter username "invalid" and password "invalid"
    And I click the login button
    Then I should see login feedback
