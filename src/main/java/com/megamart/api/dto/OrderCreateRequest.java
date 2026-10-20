package com.megamart.api.dto;

import com.megamart.dto.order.CartItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateRequest(
        @NotBlank @Size(max = 120) String recipientName,
        @NotBlank @Size(max = 24)
        @Pattern(regexp = "[+0-9() -]{8,24}", message = "Enter a valid phone number.") String phone,
        @NotBlank @Size(max = 1000) String deliveryAddress,
        @NotEmpty @Size(max = 50) List<@Valid CartItemRequest> items
) { }
