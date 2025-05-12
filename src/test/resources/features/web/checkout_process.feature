Feature: Demoblaze Checkout Process

  @web @e2e
  Scenario: Complete checkout process
    Given I am on the Demoblaze homepage
    When I log in with valid credentials
    And I click on a product named "Samsung galaxy s6"
    And I click the "Add to cart" button
    And I accept the product added popup
    And I navigate to the cart page
    Then I should see the product in the cart
    When I click the "Place Order" button
    And I fill in the order form with valid details
    And I click the "Purchase" button
    Then I should see the purchase confirmation
    And I should see the success message "Thank you for your purchase!"
    When I click the "OK" button
    Then I should return to the homepage
