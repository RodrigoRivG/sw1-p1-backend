package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.web.bind.annotation.*;
import com.rodrigo.sw1.app_sw1.dto.LoginRequest;
import com.rodrigo.sw1.app_sw1.dto.RegisterRequest;
import com.rodrigo.sw1.app_sw1.services.AuthService;

//import java.util.Optional;
//import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.rodrigo.sw1.app_sw1.models.User;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginRequest request) {
        try {
            Map<String, Object> response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
     }
}
