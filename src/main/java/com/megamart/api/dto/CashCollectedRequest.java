package com.megamart.api.dto;

import jakarta.validation.constraints.AssertTrue;

public record CashCollectedRequest(@AssertTrue boolean confirm) { }
