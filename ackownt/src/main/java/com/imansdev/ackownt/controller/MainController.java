package com.imansdev.ackownt.controller;

import com.imansdev.ackownt.dto.TransactionDTO;
import com.imansdev.ackownt.dto.UpdateUserDTO;
import com.imansdev.ackownt.dto.UserDTO;
import com.imansdev.ackownt.model.Customer;
import com.imansdev.ackownt.service.MainService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class MainController {

    @Autowired
    private MainService mainService;


    @PostMapping("/home/create")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody Customer user) {
        UserDTO createdUserDTO = mainService.createUser(user);
        return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
    }

    @PostMapping("/home/login")
    @ResponseBody
    public Map<String, String> loginUser(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        String token = mainService.generateToken(email, password);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    @PostMapping("/account/create")
    @ResponseBody
    public TransactionDTO createAccount(@RequestParam("amount") Long amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return mainService.createAccount(email, amount);
    }

    @PostMapping("/account/transaction/charge")
    @ResponseBody
    public TransactionDTO chargeAccount(@RequestParam("amount") Long amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return mainService.chargeAccount(email, amount);
    }

    @PostMapping("/account/transaction/deduction")
    @ResponseBody
    public TransactionDTO deductAmount(@RequestParam("amount") Long amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return mainService.deductAmount(email, amount);
    }

    @GetMapping("/account/transaction/list")
    @ResponseBody
    public Map<String, Object> listUserTransactions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return mainService.getUserAccountInfoAndTransactions(email);
    }

    @GetMapping("/account")
    @ResponseBody
    public UserDTO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return mainService.getUserInfo(email);
    }

    @PutMapping("/account/update")
    @ResponseBody
    public UserDTO updateUserInfo(@RequestBody UpdateUserDTO updateUserDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return mainService.updateUserInfo(email, updateUserDTO);
    }

    @DeleteMapping("/account/delete")
    @ResponseBody
    public String deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        mainService.deleteUserAndRelatedData(email);
        return "Account and related data deleted successfully";
    }
}
