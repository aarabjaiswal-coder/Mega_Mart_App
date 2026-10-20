package com.megamart.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {
    @NotBlank @Size(max = 120)
    private String recipientName;

    @NotBlank @Size(max = 24)
    @Pattern(regexp = "[+0-9() -]{8,24}", message = "Enter a valid phone number.")
    private String phone;

    @NotBlank @Size(max = 1000)
    private String deliveryAddress;

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
