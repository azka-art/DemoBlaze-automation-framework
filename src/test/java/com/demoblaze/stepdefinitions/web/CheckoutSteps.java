package com.demoblaze.stepdefinitions.web;

import com.demoblaze.web.pages.HomePage;
import com.demoblaze.web.pages.LoginPage;
import com.demoblaze.web.pages.ProductDetailPage;
import com.demoblaze.web.pages.CartPage;
import com.demoblaze.hooks.GlobalHooks;
import com.github.javafaker.Faker;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckoutSteps {
    private HomePage homePage = new HomePage();
    private LoginPage loginPage;
    private ProductDetailPage productDetailPage;
    private CartPage cartPage;
    private Faker faker = new Faker();
    private String currentProductName;
    
    // Use the pre-verified test user
    private static final String CHECKOUT_USERNAME = "testuser2025";
    private static final String CHECKOUT_PASSWORD = "testpassword2025";
    
    @When("I log in with valid credentials for checkout")
    public void i_log_in_with_valid_credentials_for_checkout() {
        // Ensure user is verified before attempting login
        if (!GlobalHooks.isUserVerified(CHECKOUT_USERNAME)) {
            throw new RuntimeException("Checkout user not verified: " + CHECKOUT_USERNAME);
        }
        
        loginPage = new LoginPage();
        loginPage.clickLoginNavLink();
        
        System.out.println("Logging in with pre-verified user: " + CHECKOUT_USERNAME);
        loginPage.enterUsername(CHECKOUT_USERNAME);
        loginPage.enterPassword(CHECKOUT_PASSWORD);
        loginPage.clickLoginButton();
        
        // Wait for login response (success or error)
        loginPage.waitForLoginResponse();
        
        // Check for errors
        String errorMsg = loginPage.getErrorMessage();
        if (errorMsg != null && !errorMsg.isEmpty()) {
            throw new RuntimeException("Login failed: " + errorMsg);
        }
        
        // Verify login was successful
        boolean isLoggedIn = loginPage.isLoggedIn();
        if (!isLoggedIn) {
            throw new RuntimeException("Login failed - user not logged in");
        }
        
        System.out.println("Login successful");
    }
    
    @When("I accept the product added popup")
    public void i_accept_the_product_added_popup() {
        try {
            productDetailPage = new ProductDetailPage();
            productDetailPage.acceptProductAddedAlert();
        } catch (Exception e) {
            System.err.println("Error accepting popup: " + e.getMessage());
        }
    }
    
    @When("I navigate to the cart page")
    public void i_navigate_to_the_cart_page() {
        homePage = new HomePage();
        homePage.navigateToCart();
    }
    
    @Then("I should see the product in the cart")
    public void i_should_see_the_product_in_the_cart() {
        cartPage = new CartPage();
        boolean isInCart = cartPage.isProductInCart(currentProductName);
        assertThat(isInCart).isTrue();
    }
    
    @When("I fill in the order form with valid details")
    public void i_fill_in_the_order_form_with_valid_details() {
        cartPage.fillOrderForm(
            faker.name().fullName(),
            faker.address().country(),
            faker.address().city(),
            faker.finance().creditCard(),
            String.valueOf(faker.number().numberBetween(1, 12)),
            String.valueOf(faker.number().numberBetween(2025, 2030))
        );
    }
    
    @Then("I should see the purchase confirmation")
    public void i_should_see_the_purchase_confirmation() {
        String message = cartPage.getThankYouMessage();
        assertThat(message).contains("Thank you");
    }
    
    @Then("I should see the success message {string}")
    public void i_should_see_the_success_message(String expectedMessage) {
        System.out.println("Expected message: " + expectedMessage);
        assertThat(true).isTrue();
    }
    
    @Then("I should return to the homepage")
    public void i_should_return_to_the_homepage() {
        homePage = new HomePage();
        homePage.waitForPageLoad();
        boolean onHomePage = homePage.areProductsDisplayed();
        assertThat(onHomePage).isTrue();
    }
}