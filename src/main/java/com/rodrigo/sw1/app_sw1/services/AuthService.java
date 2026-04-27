package com.rodrigo.sw1.app_sw1.services;

import com.rodrigo.sw1.app_sw1.dto.LoginRequest;
import com.rodrigo.sw1.app_sw1.dto.RegisterRequest;
import com.rodrigo.sw1.app_sw1.models.User;
import com.rodrigo.sw1.app_sw1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.config.JwtService;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "employee");
        user.setDepartmentId(request.getDepartmentId());

        return userRepository.save(user);
    }

    public Map<String, Object> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword()))  {
            String token = jwtService.generateToken(
                userOpt.get().getEmail(),
                userOpt.get().getRole(),
                userOpt.get().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", userOpt.get().getRole());
            response.put("name", userOpt.get().getName());
            response.put("id", userOpt.get().getId());

            return response;
        }

        throw new RuntimeException("Credenciales inválidas");
    }
}
