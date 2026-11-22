package com.megamart.api.controller;

import com.megamart.service.SupabaseAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CustomerAuthController {
    private final SupabaseAuthService authService;

    public CustomerAuthController(SupabaseAuthService authService) { this.authService = authService; }

    @GetMapping("/config")
    public SupabaseAuthService.PublicConfig config() { return authService.publicConfig(); }
}
