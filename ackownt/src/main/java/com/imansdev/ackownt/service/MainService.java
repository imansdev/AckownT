package com.imansdev.ackownt.service;

import com.imansdev.ackownt.auth.JwtUtil;
import com.imansdev.ackownt.dto.AccountDTO;
import com.imansdev.ackownt.dto.TransactionDTO;
import com.imansdev.ackownt.dto.UpdateUserDTO;
import com.imansdev.ackownt.dto.UserDTO;
import com.imansdev.ackownt.enums.TransactionDescription;
import com.imansdev.ackownt.enums.TransactionStatus;
import com.imansdev.ackownt.enums.TransactionType;
import com.imansdev.ackownt.model.Account;
import com.imansdev.ackownt.model.Transaction;
import com.imansdev.ackownt.model.Customer;
import com.imansdev.ackownt.repository.AccountRepository;
import com.imansdev.ackownt.repository.TransactionRepository;
import com.imansdev.ackownt.repository.CustomerRepository;
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
import java.time.LocalDate;

@Service
public class MainService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
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
    public UserDTO createUser(Customer user) {
        validateUniqueUserFields(user);
        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        customerRepository.save(user);
        return convertToUserDTO(user);
    }

    // Create a new Account for a User
    @Transactional
    public TransactionDTO createAccount(String email, Long amount) {
        validateAmountIsPositive(amount);
        validateAmountGreaterThanMinBalance(amount);
        Customer user = getUserByEmail(email);
        validateUserAccountDoesNotExist(user);
        Account account = createNewAccount(user, amount);
        Transaction transaction = recordTransaction(user, account, amount, TransactionType.CHARGE,
                TransactionDescription.CHARGING_SUCCESSFUL);
        return convertToTransactionDTO(transaction);
    }

    // Charge an account
    @Transactional
    public TransactionDTO chargeAccount(String email, Long amount) {
        validateAmountIsPositive(amount);
        Customer user = getUserByEmail(email);
        Account account = getUserAccount(user);
        TransactionDescriptionForCharge(account, amount);
        Transaction transaction = recordTransaction(user, account, amount, TransactionType.CHARGE,
                TransactionDescription.CHARGING_SUCCESSFUL);
        return convertToTransactionDTO(transaction);
    }

    // Deduct an amount from the account
    @Transactional
    public TransactionDTO deductAmount(String email, Long amount) {
        validateAmountIsPositive(amount);
        validateWithdrawalAmount(amount);
        Customer user = getUserByEmail(email);
        Account account = getUserAccount(user);
        validateSufficientBalance(account, amount);
        validateDailyDeductions(user, amount);
        updateAccountBalanceForDeduction(account, amount);
        Transaction transaction = recordTransaction(user, account, amount,
                TransactionType.DEDUCTION, TransactionDescription.DEDUCTION_SUCCESSFUL);
        return convertToTransactionDTO(transaction);
    }

    // Get user account information and transactions
    public Map<String, Object> getUserAccountInfoAndTransactions(String email) {
        Customer user = getUserByEmail(email);
        Account account = getUserAccount(user);
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("account", convertToAccountDTO(account));
        response.put("transactions", transactions.stream().map(this::convertToTransactionDTO)
                .collect(Collectors.toList()));
        return response;
    }

    // Get user info
    public UserDTO getUserInfo(String email) {
        Customer user = getUserByEmail(email);
        return convertToUserDTO(user);
    }

    // Update user info
    @Transactional
    public UserDTO updateUserInfo(String email, UpdateUserDTO updateUserDTO) {
        Customer user = getUserByEmail(email);
        validateUniquePhoneNumber(user, updateUserDTO.getPhoneNumber());
        updateUserDetails(user, updateUserDTO);

        // Update password if it's provided
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updateUserDTO.getPassword());
            user.setPassword(encodedPassword);
        }
        validateUser(user);
        customerRepository.save(user);
        return convertToUserDTO(user);
    }

    // Delete user and related data
    @Transactional
    public void deleteUserAndRelatedData(String email) {
        Customer user = getUserByEmail(email);
        transactionRepository.deleteByUserId(user.getId());
        customerRepository.deleteById(user.getId());
    }

    // Authenticate user and generate JWT token
    public String generateToken(String email, String password) {
        Customer user = authenticateUser(email, password);
        return jwtUtil.generateToken(user.getEmail());
    }

    // Authenticate user
    public Customer authenticateUser(String email, String password) {
        Customer user = getUserByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Invalid password");
        }
        return user;
    }

    // ----- Helper Methods -----
    private Account getUserAccount(Customer user) {
        return accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ValidationException("User's account not found"));
    }

    // Helper method to validate user existence
    private Customer getUserByEmail(String email) {
        return customerRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Helper method to validate if user exists based on national ID, email, and phone number
    private void validateUniqueUserFields(Customer user) {
        if (customerRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("Email must be unique");
        }
        if (customerRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new ValidationException("Phone number must be unique");
        }
        if (customerRepository.findByNationalId(user.getNationalId()).isPresent()) {
            throw new ValidationException("National ID must be unique");
        }
    }

    private void validateUniquePhoneNumber(Customer user, String phoneNumber) {
        customerRepository.findByPhoneNumber(phoneNumber).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new ValidationException("Phone number must be unique");
            }
        });
    }

    private void validateUserAccountDoesNotExist(Customer user) {
        if (accountRepository.findByUserId(user.getId()).isPresent()) {
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

    private void validateDailyDeductions(Customer user, Long amount) {
        LocalDate today = LocalDate.now();
        Long totalDeductionsToday =
                transactionRepository.findTotalDailyDeductions(user.getId(), today);

        if (totalDeductionsToday + amount > maxWithdrawal) {
            throw new ValidationException(
                    "Total daily deductions must be less than " + maxWithdrawal);
        }
    }

    private void validateSufficientBalance(Account account, Long amount) {
        if (account.getBalance() - minBalance < amount) {
            throw new ValidationException("Insufficient balance for this deduction");
        }
    }

    private Account createNewAccount(Customer user, Long amount) {
        Account account = new Account();
        account.setUser(user);
        account.setBalance(amount);
        accountRepository.save(account);
        return account;
    }

    private void TransactionDescriptionForCharge(Account account, Long amount) {
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    private void updateAccountBalanceForDeduction(Account account, Long amount) {
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    private Transaction recordTransaction(Customer user, Account account, Long amount,
            TransactionType type, TransactionDescription description) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionName(type);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setWithdrawalBalance(account.getBalance() - minBalance);
        transactionRepository.save(transaction);
        return transaction;
    }

    private void updateUserDetails(Customer user, UpdateUserDTO updateUserDTO) {
        user.setName(updateUserDTO.getName());
        user.setSurname(updateUserDTO.getSurname());
        user.setPhoneNumber(updateUserDTO.getPhoneNumber());
        user.setMilitaryStatus(updateUserDTO.getMilitaryStatus());
    }

    private void validateUser(Customer user) {
        Set<ConstraintViolation<Customer>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder vio = new StringBuilder();
            for (ConstraintViolation<Customer> violation : violations) {
                vio.append(violation.getMessage()).append(", ");
            }
            throw new ValidationException(vio.toString());
        }

    }

    // DTO Converters
    private UserDTO convertToUserDTO(Customer user) {
        return new UserDTO(user.getName(), user.getSurname(), user.getNationalId(),
                user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null,
                user.getEmail(), user.getPhoneNumber(),
                user.getGender() != null ? user.getGender().toString() : null,
                user.getMilitaryStatus() != null ? user.getMilitaryStatus().toString() : null);
    }

    private TransactionDTO convertToTransactionDTO(Transaction transaction) {
        return new TransactionDTO(transaction.getTransactionName().toString(),
                transaction.getTransactionStatus().toString(), transaction.getAmount(),
                transaction.getTrackingNumber(), transaction.getTransactionDate(),
                transaction.getDescription().toString(), transaction.getWithdrawalBalance());
    }

    private AccountDTO convertToAccountDTO(Account account) {
        return new AccountDTO(account.getAccountNumber(), account.getBalance(),
                account.getAccountCreationDate());
    }
}
