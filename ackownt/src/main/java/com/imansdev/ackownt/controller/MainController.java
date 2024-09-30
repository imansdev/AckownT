package com.imansdev.ackownt.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.imansdev.ackownt.dto.UserDTO;
import com.imansdev.ackownt.model.Users;
import com.imansdev.ackownt.service.MainService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class MainController {

    @Autowired
    private MainService mainService;

    @PostMapping("/home/create")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody Users user) {
        UserDTO createdUserDTO = mainService.createUser(user);
        return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
    }

    @PostMapping("/home/login")
    @ResponseBody
    public Map<String, String> loginUser(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        String token = mainService.GenerateToken(email, password);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }
}
