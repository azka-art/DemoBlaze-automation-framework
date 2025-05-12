package com.demoblaze.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

public class CartPage extends BasePage {
    
    @FindBy(css = ".success")
    private List<WebElement> cartItems;
    
    @FindBy(css = "button[data-target='#orderModal']")
    private WebElement placeOrderButton;
    
    @FindBy(id = "name")
    private WebElement nameField;
    
    @FindBy(id = "country")
    private WebElement countryField;
    
    @FindBy(id = "city")
    private WebElement cityField;
    
    @FindBy(id = "card")
    private WebElement cardField;
    
    @FindBy(id = "month")
    private WebElement monthField;
    
    @FindBy(id = "year")
    private WebElement yearField;
    
    @FindBy(css = "button[onclick='purchaseOrder()']")
    private WebElement purchaseButton;
    
    @FindBy(css = ".lead.text-muted")
    private WebElement thankYouMessage;
    
    @FindBy(css = "button.confirm.btn.btn-lg.btn-primary")
    private WebElement okButton;
    
    public boolean isProductInCart(String productName) {
        wait.until(ExpectedConditions.visibilityOfAllElements(cartItems));
        return cartItems.stream()
            .anyMatch(item -> item.getText().contains(productName));
    }
    
    public void clickPlaceOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(placeOrderButton));
        clickElement(placeOrderButton);
        wait.until(ExpectedConditions.visibilityOf(nameField));
    }
    
    public void fillOrderForm(String name, String country, String city, 
                            String card, String month, String year) {
        enterText(nameField, name);
        enterText(countryField, country);
        enterText(cityField, city);
        enterText(cardField, card);
        enterText(monthField, month);
        enterText(yearField, year);
    }
    
    public void clickPurchase() {
        clickElement(purchaseButton);
        wait.until(ExpectedConditions.visibilityOf(thankYouMessage));
    }
    
    public String getThankYouMessage() {
        return getElementText(thankYouMessage);
    }
    
    public void clickOkButton() {
        clickElement(okButton);
    }
}
