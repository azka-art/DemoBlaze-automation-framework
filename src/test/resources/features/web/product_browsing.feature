Feature: Demoblaze Product Browsing

  @web @smoke
  Scenario: Browse products by categories
    Given I am on the Demoblaze homepage
    When I click on "Phones" category
    Then I should see products filtered by Phones category
    When I click on "Laptops" category
    Then I should see products filtered by Laptops category
    When I click on "Monitors" category
    Then I should see products filtered by Monitors category

  @web @functional
  Scenario: View product details
    Given I am on the Demoblaze homepage
    When I click on "Phones" category
    And I click on product "Samsung galaxy s6"
    Then I should see the product details page
    And I should see product name "Samsung galaxy s6"
    And I should see product price

  @web @functional
  Scenario: Navigate between pages
    Given I am on the Demoblaze homepage
    When I click the "Next" button
    Then I should see different products
    When I click the "Previous" button
    Then I should see the first page products

  @web @functional
  Scenario: Search for non-existent product
    Given I am on the Demoblaze homepage
    When I try to find product "NonExistentProduct123"
    Then I should not find any matching products

  @web @e2e
  Scenario: Add product to cart
    Given I am on the Demoblaze homepage
    When I click on product "Nokia lumia 1520"
    And I click "Add to cart" button
    And I accept the product added confirmation
    And I navigate to cart
    Then I should see "Nokia lumia 1520" in my cart