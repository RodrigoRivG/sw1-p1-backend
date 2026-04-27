package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.repository.UserRepository;
import com.rodrigo.sw1.app_sw1.models.User;

import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getByEmail (String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
