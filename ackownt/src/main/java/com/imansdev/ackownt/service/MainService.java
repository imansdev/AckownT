package com.imansdev.ackownt.service;

import com.imansdev.ackownt.auth.JwtUtil;
import com.imansdev.ackownt.dto.AccountDTO;
import com.imansdev.ackownt.dto.TransactionDTO;
import com.imansdev.ackownt.dto.UpdateUserDTO;
import com.imansdev.ackownt.dto.UserDTO;
import com.imansdev.ackownt.enums.TransactionDescription;
import com.imansdev.ackownt.enums.TransactionStatus;
import com.imansdev.ackownt.enums.TransactionType;
import com.imansdev.ackownt.model.Accounts;
import com.imansdev.ackownt.model.Transactions;
import com.imansdev.ackownt.model.Users;
import com.imansdev.ackownt.repository.AccountsRepository;
import com.imansdev.ackownt.repository.TransactionsRepository;
import com.imansdev.ackownt.repository.UsersRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private Validator validator;

    @Value("${account.minBalance}")
    private long minBalance;
    @Value("${account.maxWithdrawal}")
    private long maxWithdrawal;
    @Value("${account.minWithdrawal}")
    private long minWithdrawal;

    // Create a new User
    @Transactional
    public UserDTO createUser(Users user) {
        validateUniqueUserFields(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
        return convertToUserDTO(user);
    }

    // Create a new Account for a User
    @Transactional
    public TransactionDTO createAccount(String email, Long amount) {
        validateAmountGreaterThanMinBalance(amount);
        Users user = getUserByEmail(email);
        validateUserAccountDoesNotExist(user);
        Accounts account = createNewAccount(user, amount);
        Transactions transaction = recordTransaction(user, account, amount, TransactionType.CHARGE,
                TransactionDescription.CHARGING_SUCCESSFUL);
        return convertToTransactionDTO(transaction);
    }

    // Charge an account
    @Transactional
    public TransactionDTO chargeAccount(String email, Long amount) {
        validateAmountIsPositive(amount);
        Users user = getUserByEmail(email);
        Accounts account = getUserAccount(user);
        TransactionDescriptionForCharge(account, amount);
        Transactions transaction = recordTransaction(user, account, amount, TransactionType.CHARGE,
                TransactionDescription.CHARGING_SUCCESSFUL);
        return convertToTransactionDTO(transaction);
    }

    // Deduct an amount from the account
    @Transactional
    public TransactionDTO deductAmount(String email, Long amount) {
        validateWithdrawalAmount(amount);
        Users user = getUserByEmail(email);
        Accounts account = getUserAccount(user);
        validateSufficientBalance(account, amount);
        updateAccountBalanceForDeduction(account, amount);
        Transactions transaction = recordTransaction(user, account, amount,
                TransactionType.DEDUCTION, TransactionDescription.DEDUCTION_SUCCESSFUL);
        return convertToTransactionDTO(transaction);
    }

    // Get user account information and transactions
    public Map<String, Object> getUserAccountInfoAndTransactions(String email) {
        Users user = getUserByEmail(email);
        Accounts account = getUserAccount(user);
        List<Transactions> transactions = transactionsRepository.findByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("account", convertToAccountDTO(account));
        response.put("transactions", transactions.stream().map(this::convertToTransactionDTO)
                .collect(Collectors.toList()));
        return response;
    }

    // Get user info
    public UserDTO getUserInfo(String email) {
        Users user = getUserByEmail(email);
        return convertToUserDTO(user);
    }

    // Update user info
    @Transactional
    public UserDTO updateUserInfo(String email, UpdateUserDTO updateUserDTO) {
        Users user = getUserByEmail(email);
        validateUniquePhoneNumber(user, updateUserDTO.getPhoneNumber());
        updateUserDetails(user, updateUserDTO);
        validateAndSaveUser(user);
        return convertToUserDTO(user);
    }

    // Delete user and related data
    @Transactional
    public void deleteUserAndRelatedData(String email) {
        Users user = getUserByEmail(email);
        transactionsRepository.deleteByUserId(user.getId());
        usersRepository.deleteById(user.getId());
    }

    // Authenticate user and generate JWT token
    public String generateToken(String email, String password) {
        Users user = authenticateUser(email, password);
        return jwtUtil.generateToken(user.getEmail());
    }

    // Authenticate user
    public Users authenticateUser(String email, String password) {
        Users user = getUserByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Invalid password");
        }
        return user;
    }

    // ----- Helper Methods -----
    private Accounts getUserAccount(Users user) {
        return accountsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ValidationException("User's account not found"));
    }

    // Helper method to validate user existence
    private Users getUserByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Helper method to validate if user exists based on national ID, email, and phone number
    private void validateUniqueUserFields(Users user) {
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("Email must be unique");
        }
        if (usersRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new ValidationException("Phone number must be unique");
        }
        if (usersRepository.findByNationalId(user.getNationalId()).isPresent()) {
            throw new ValidationException("National ID must be unique");
        }
    }

    private void validateUniquePhoneNumber(Users user, String phoneNumber) {
        usersRepository.findByPhoneNumber(phoneNumber).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new ValidationException("Phone number must be unique");
            }
        });
    }

    private void validateUserAccountDoesNotExist(Users user) {
        if (accountsRepository.findByUserId(user.getId()).isPresent()) {
            throw new ValidationException("Account already exists for the user");
        }
    }

    private void validateAmountGreaterThanMinBalance(Long amount) {
        if (amount == null || amount <= minBalance) {
            throw new ValidationException("Initial deposit must be greater than " + minBalance);
        }
    }

    private void validateAmountIsPositive(Long amount) {
        if (amount == null || amount <= 0) {
            throw new ValidationException("Amount must be a positive number");
        }
    }

    private void validateWithdrawalAmount(Long amount) {
        if (amount == null || amount <= minWithdrawal || amount >= maxWithdrawal) {
            throw new ValidationException(
                    "Amount must be between " + minWithdrawal + " and " + maxWithdrawal);
        }
    }

    private void validateSufficientBalance(Accounts account, Long amount) {
        if (account.getBalance() < amount - minBalance) {
            throw new ValidationException("Insufficient balance for this deduction");
        }
    }

    private Accounts createNewAccount(Users user, Long amount) {
        Accounts account = new Accounts();
        account.setUser(user);
        account.setBalance(amount);
        accountsRepository.save(account);
        return account;
    }

    private void TransactionDescriptionForCharge(Accounts account, Long amount) {
        account.setBalance(account.getBalance() + amount);
        accountsRepository.save(account);
    }

    private void updateAccountBalanceForDeduction(Accounts account, Long amount) {
        account.setBalance(account.getBalance() - amount);
        accountsRepository.save(account);
    }

    private Transactions recordTransaction(Users user, Accounts account, Long amount,
            TransactionType type, TransactionDescription description) {
        Transactions transaction = new Transactions();
        transaction.setUser(user);
        transaction.setTransactionName(type);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setWithdrawalBalance(account.getBalance() - minBalance);
        transactionsRepository.save(transaction);
        return transaction;
    }

    private void updateUserDetails(Users user, UpdateUserDTO updateUserDTO) {
        user.setName(updateUserDTO.getName());
        user.setSurname(updateUserDTO.getSurname());
        user.setPhoneNumber(updateUserDTO.getPhoneNumber());
        user.setMilitaryStatus(updateUserDTO.getMilitaryStatus());
    }

    private void validateAndSaveUser(Users user) {
        Set<ConstraintViolation<Users>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder vio = new StringBuilder();
            for (ConstraintViolation<Users> violation : violations) {
                vio.append(violation.getMessage()).append(", ");
            }
            throw new ValidationException(vio.toString());
        }
        usersRepository.save(user);
    }

    // DTO Converters
    private UserDTO convertToUserDTO(Users user) {
        return new UserDTO(user.getName(), user.getSurname(), user.getNationalId(),
                user.getDateOfBirth().toString(), user.getEmail(), user.getPhoneNumber(),
                user.getGender().toString(), user.getMilitaryStatus().toString());
    }

    private TransactionDTO convertToTransactionDTO(Transactions transaction) {
        return new TransactionDTO(transaction.getTransactionName().toString(),
                transaction.getTransactionStatus().toString(), transaction.getAmount(),
                transaction.getTrackingNumber(), transaction.getTransactionDate(),
                transaction.getDescription().toString(), transaction.getWithdrawalBalance());
    }

    private AccountDTO convertToAccountDTO(Accounts account) {
        return new AccountDTO(account.getAccountNumber(), account.getBalance(),
                account.getAccountCreationDate());
    }
}
