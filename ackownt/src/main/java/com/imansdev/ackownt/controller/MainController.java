package com.imansdev.ackownt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
