package com.imansdev.ackownt.service;

import com.imansdev.ackownt.auth.JwtUtil;
import com.imansdev.ackownt.dto.AccountDTO;
import com.imansdev.ackownt.dto.TransactionDTO;
import com.imansdev.ackownt.dto.UpdateUserDTO;
import com.imansdev.ackownt.dto.UserDTO;
import com.imansdev.ackownt.enums.TransactionDescription;
import com.imansdev.ackownt.enums.TransactionStatus;
import com.imansdev.ackownt.enums.TransactionType;
import com.imansdev.ackownt.exception.CustomServiceException;
import com.imansdev.ackownt.model.Accounts;
import com.imansdev.ackownt.model.Transactions;
import com.imansdev.ackownt.model.Users;
import com.imansdev.ackownt.repository.AccountsRepository;
import com.imansdev.ackownt.repository.TransactionsRepository;
import com.imansdev.ackownt.repository.UsersRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public TransactionDTO createAccount(String email, Long amount) {
        if (amount == null || amount <= minBalance) {
            throw new ValidationException("Initial deposit must be greater than " + minBalance);
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new CustomServiceException("User not found"));

        Accounts existingAccount = accountsRepository.findByUserId(user.getId()).orElse(null);
        if (existingAccount != null) {
            throw new ValidationException("Account already exists for the user");
        }

        Accounts account = new Accounts();
        account.setUser(user);
        account.setBalance(amount);
        accountsRepository.save(account);

        Transactions transaction = new Transactions();
        transaction.setUser(user);
        transaction.setTransactionName(TransactionType.CHARGE);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setAmount(amount);
        transaction.setDescription(TransactionDescription.CHARGING_SUCCESSFUL);
        transaction.setWithdrawalBalance(account.getBalance() - minBalance);

        transactionsRepository.save(transaction);

        return new TransactionDTO(transaction.getTransactionName().toString(),
                transaction.getTransactionStatus().toString(), transaction.getAmount(),
                transaction.getTrackingNumber(), transaction.getTransactionDate(),
                transaction.getDescription().toString(), transaction.getWithdrawalBalance());
    }

    @Transactional
    public TransactionDTO chargeAccount(String email, Long amount) {
        if (amount == null || amount <= 0) {
            throw new ValidationException("Amount must be a positive number");
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new CustomServiceException("User not found"));

        Accounts account = accountsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ValidationException(
                        "User's account not found. Please create an account first."));

        account.setBalance(account.getBalance() + amount);
        accountsRepository.save(account);

        Transactions transaction = new Transactions();
        transaction.setUser(user);
        transaction.setTransactionName(TransactionType.CHARGE);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setAmount(amount);
        transaction.setDescription(TransactionDescription.CHARGING_SUCCESSFUL);
        transaction.setWithdrawalBalance(account.getBalance() - minBalance);

        transactionsRepository.save(transaction);

        return new TransactionDTO(transaction.getTransactionName().toString(),
                transaction.getTransactionStatus().toString(), transaction.getAmount(),
                transaction.getTrackingNumber(), transaction.getTransactionDate(),
                transaction.getDescription().toString(), transaction.getWithdrawalBalance());
    }

    @Transactional
    public TransactionDTO deductAmount(String email, Long amount) {
        if (amount == null || amount <= minWithdrawal || amount >= maxWithdrawal) {
            throw new ValidationException(
                    "Amount must be between " + minWithdrawal + " and " + maxWithdrawal);
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        Accounts account = accountsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ValidationException("User's account not found"));


        LocalDate today = LocalDate.now();
        Long totalDeductionsToday =
                transactionsRepository.findTotalDailyDeductions(user.getId(), today);

        if (totalDeductionsToday + amount > maxWithdrawal) {
            throw new ValidationException(
                    "Total daily deductions must be less than " + maxWithdrawal);
        }

        if (account.getBalance() < (amount - minBalance)) {
            throw new ValidationException("Insufficient balance for this deduction");
        }


        account.setBalance(account.getBalance() - amount);
        accountsRepository.save(account);

        Transactions transaction = new Transactions();
        transaction.setUser(user);
        transaction.setTransactionName(TransactionType.DEDUCTION);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setAmount(amount);
        transaction.setDescription(TransactionDescription.DEDUCTION_SUCCESSFUL);
        transaction.setWithdrawalBalance(account.getBalance() - minBalance);

        transactionsRepository.save(transaction);

        return new TransactionDTO(transaction.getTransactionName().toString(),
                transaction.getTransactionStatus().toString(), transaction.getAmount(),
                transaction.getTrackingNumber(), transaction.getTransactionDate(),
                transaction.getDescription().toString(), transaction.getWithdrawalBalance());
    }

    public Map<String, Object> getUserAccountInfoAndTransactions(String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        Accounts account = accountsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ValidationException("User's account not found"));

        List<Transactions> transactions = transactionsRepository.findByUserId(user.getId());

        AccountDTO accountDTO = new AccountDTO(account.getAccountNumber(), account.getBalance(),
                account.getAccountCreationDate());

        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transaction -> new TransactionDTO(transaction.getTransactionName().toString(),
                        transaction.getTransactionStatus().toString(), transaction.getAmount(),
                        transaction.getTrackingNumber(), transaction.getTransactionDate(),
                        transaction.getDescription().toString(),
                        transaction.getWithdrawalBalance()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("account", accountDTO);
        response.put("transactions", transactionDTOs);

        return response;
    }

    public UserDTO getUserInfo(String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        return new UserDTO(user.getName(), user.getSurname(), user.getNationalId(),
                user.getDateOfBirth().toString(), user.getEmail(), user.getPhoneNumber(),
                user.getGender().toString(), user.getMilitaryStatus().toString());
    }

    @Transactional
    public UserDTO updateUserInfo(String email, UpdateUserDTO updateUserDTO) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        // Check if the new phone number exists for another user
        usersRepository.findByPhoneNumber(updateUserDTO.getPhoneNumber()).ifPresent(u -> {
            if (!u.getId().equals(user.getId())) {
                throw new ValidationException("Phone number must be unique");
            }
        });

        // Update the user details with the provided information
        user.setName(updateUserDTO.getName());
        user.setSurname(updateUserDTO.getSurname());
        user.setPhoneNumber(updateUserDTO.getPhoneNumber());
        user.setMilitaryStatus(updateUserDTO.getMilitaryStatus());

        Set<ConstraintViolation<Users>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder vio = new StringBuilder();
            for (ConstraintViolation<Users> violation : violations) {
                vio.append(violation.getMessage()).append(", ");
            }
            throw new ValidationException(vio.toString());
        }

        usersRepository.save(user);

        // Return updated user information as a DTO
        return new UserDTO(user.getName(), user.getSurname(), user.getNationalId(),
                user.getDateOfBirth().toString(), user.getEmail(), user.getPhoneNumber(),
                user.getGender().toString(), user.getMilitaryStatus().toString());
    }

    @Transactional
    public void deleteUserAndRelatedData(String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        transactionsRepository.deleteByUserId(user.getId());

        usersRepository.deleteById(user.getId());
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
