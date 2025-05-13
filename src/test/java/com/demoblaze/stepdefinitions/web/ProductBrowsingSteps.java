package com.demoblaze.stepdefinitions.web;

import com.demoblaze.web.pages.HomePage;
import com.demoblaze.web.pages.ProductDetailPage;
import com.demoblaze.web.pages.CartPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductBrowsingSteps {
    private HomePage homePage;
    private ProductDetailPage productDetailPage;
    private CartPage cartPage;
    
    public ProductBrowsingSteps() {
        this.homePage = new HomePage();
    }
    
    @When("I click on {string} category")
    public void iClickOnCategory(String category) {
        if (homePage == null) {
            homePage = new HomePage();
        }
        
        switch(category.toLowerCase()) {
            case "phones":
                homePage.clickPhonesCategory();
                break;
            case "laptops":
                homePage.clickLaptopsCategory();
                break;
            case "monitors":
                homePage.clickMonitorsCategory();
                break;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }
    
    @Then("I should see products filtered by {word} category")
    public void iShouldSeeProductsFilteredByCategory(String category) {
        boolean productsDisplayed = homePage.areProductsDisplayed();
        assertThat(productsDisplayed)
            .as("Products should be displayed for " + category)
            .isTrue();
    }
    
    @When("I click on product {string}")
    public void iClickOnProduct(String productName) {
        homePage.clickProductByName(productName);
        productDetailPage = new ProductDetailPage();
    }
    
    @Then("I should see the product details page")
    public void iShouldSeeTheProductDetailsPage() {
        String productName = productDetailPage.getProductName();
        assertThat(productName)
            .as("Product details page should be displayed")
            .isNotNull()
            .isNotEmpty();
    }
    
    @Then("I should see product name {string}")
    public void iShouldSeeProductName(String expectedName) {
        String actualName = productDetailPage.getProductName();
        assertThat(actualName)
            .as("Product name")
            .contains(expectedName);
    }
    
    @Then("I should see product price")
    public void iShouldSeeProductPrice() {
        String price = productDetailPage.getProductPrice();
        assertThat(price)
            .as("Product price should be displayed")
            .isNotNull()
            .isNotEmpty()
            .contains("$");
    }
    
    @Then("I should see different products")
    public void iShouldSeeDifferentProducts() {
        try {
            Thread.sleep(2000);
            boolean productsDisplayed = homePage.areProductsDisplayed();
            assertThat(productsDisplayed)
                .as("Different products should be displayed")
                .isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Then("I should see the first page products")
    public void iShouldSeeTheFirstPageProducts() {
        boolean productsDisplayed = homePage.areProductsDisplayed();
        assertThat(productsDisplayed)
            .as("First page products should be displayed")
            .isTrue();
    }
    
    @When("I try to find product {string}")
    public void iTryToFindProduct(String productName) {
        try {
            homePage.clickProductByName(productName);
        } catch (Exception e) {
            // Product not found - this is expected for this test
        }
    }
    
    @Then("I should not find any matching products")
    public void iShouldNotFindAnyMatchingProducts() {
        boolean onHomePage = homePage.areProductsDisplayed();
        assertThat(onHomePage)
            .as("Should still be on homepage (product not found)")
            .isTrue();
    }
    
    @When("I click {string} button")
    public void iClickButton(String buttonName) {
        if (buttonName.equalsIgnoreCase("Add to cart")) {
            productDetailPage.clickAddToCart();
        }
    }
    
    @When("I accept the product added confirmation")
    public void iAcceptTheProductAddedConfirmation() {
        productDetailPage.acceptProductAddedAlert();
    }
    
    @When("I navigate to cart")
    public void iNavigateToCart() {
        homePage = new HomePage();
        homePage.navigateToCart();
        cartPage = new CartPage();
    }
    
    @Then("I should see {string} in my cart")
    public void iShouldSeeInMyCart(String productName) {
        boolean isInCart = cartPage.isProductInCart(productName);
        assertThat(isInCart)
            .as(productName + " should be in cart")
            .isTrue();
    }
}
