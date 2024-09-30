package com.imansdev.ackownt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.imansdev.ackownt.auth.JwtUtil;
import com.imansdev.ackownt.dto.UserDTO;
import com.imansdev.ackownt.model.Users;
import com.imansdev.ackownt.repository.AccountsRepository;
import com.imansdev.ackownt.repository.TransactionsRepository;
import com.imansdev.ackownt.repository.UsersRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;

@Service
public class MainService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public UserDTO createUser(Users user) {
        // Check for unique email
        usersRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new ValidationException("Email must be unique");
        });



        // Check for unique phone number
        usersRepository.findByPhoneNumber(user.getPhoneNumber()).ifPresent(u -> {
            throw new ValidationException("Phone number must be unique");
        });



        // Check for unique national ID
        usersRepository.findByNationalId(user.getNationalId()).ifPresent(u -> {
            throw new ValidationException("National ID must be unique");
        });


        user.setPassword(passwordEncoder.encode(user.getPassword()));

        usersRepository.save(user);
        return new UserDTO(user.getName(), user.getSurname(), user.getNationalId(),
                user.getDateOfBirth().toString(), user.getEmail(), user.getPhoneNumber(),
                user.getGender().toString(), user.getMilitaryStatus().toString());
    }



    public Users authenticateUser(String email, String password) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid email"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            throw new ValidationException("Invalid password");
        }
    }

    public String GenerateToken(String email, String password) {
        Users user = authenticateUser(email, password);
        return jwtUtil.generateToken(user.getEmail());
    }
}
