package com.insurancetech.controller;

import com.insurancetech.model.User;
import com.insurancetech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporary test controller - will be removed later
 */
@RestController
@RequestMapping("/api/test")
public class UserTestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/debug")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        List<User> users = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("count", users.size());

        if (!users.isEmpty()) {
            User firstUser = users.get(0);
            response.put("firstUserEmail", firstUser.getEmail());
            response.put("firstUserFirstName", firstUser.getFirstName());
            response.put("firstUserLastName", firstUser.getLastName());
            response.put("firstUserRole", firstUser.getRole());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUserCount() {
        long count = userRepository.count();
        return ResponseEntity.ok(count);
    }
}
