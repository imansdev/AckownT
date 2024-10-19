package com.imansdev.ackownt.service;

import com.imansdev.ackownt.auth.JwtUtil;
import com.imansdev.ackownt.dto.UpdateUserDTO;
import com.imansdev.ackownt.enums.Gender;
import com.imansdev.ackownt.enums.MilitaryStatus;
import com.imansdev.ackownt.model.Users;
import com.imansdev.ackownt.repository.AccountsRepository;
import com.imansdev.ackownt.repository.TransactionsRepository;
import com.imansdev.ackownt.repository.UsersRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MainServiceTest {

    @InjectMocks
    private MainService mainService;

    @Mock
    private Validator validator;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_SuccessfulCreation() {

        Users newUser = new Users();
        newUser.setName("iman");
        newUser.setSurname("abc");
        newUser.setNationalId("4528422034");
        newUser.setDateOfBirth(LocalDate.parse("2000-02-02"));
        newUser.setEmail("imanabc@example.com");
        newUser.setPhoneNumber("09129966331");
        newUser.setPassword("password123");
        newUser.setGender(Gender.MALE);
        newUser.setMilitaryStatus(MilitaryStatus.COMPLETED_SERVICE);

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(validator.validate(any(Users.class))).thenReturn(Set.of()); // Mocking validation

        mainService.createUser(newUser);

        verify(usersRepository, times(1)).save(any(Users.class));
    }

    @Test
    void testLoginUser_SuccessfulLogin() {

        Users user = new Users();
        user.setEmail("imanabc@example.com");
        user.setPassword("encodedPassword");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("imanabc@example.com")).thenReturn("mockToken");

        String token = mainService.generateToken("imanabc@example.com", "password123");

        assertNotNull(token);
        assertEquals("mockToken", token);
    }


    // --- Invalid Create User Tests ---

    @Test
    void testCreateUser_InvalidDate() {
        // Create a Users object with an invalid date format (future date)
        Users invalidUser = new Users();
        invalidUser.setName("iman");
        invalidUser.setSurname("abc");
        invalidUser.setNationalId("4528422034");
        invalidUser.setDateOfBirth(LocalDate.now().plusDays(1)); // Future date
        invalidUser.setEmail("imanabc@example.com");
        invalidUser.setPhoneNumber("09129966331");
        invalidUser.setPassword("password123");
        invalidUser.setGender(Gender.MALE);
        invalidUser.setMilitaryStatus(MilitaryStatus.COMPLETED_SERVICE);

        ConstraintViolation<Users> violation = mock(ConstraintViolation.class);
        when(violation.getMessage())
                .thenReturn("The date of birth is required and must be in the past");
        when(validator.validate(invalidUser)).thenReturn(Set.of(violation));


        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createUser(invalidUser);
        });

        assertTrue(exception.getMessage()
                .contains("The date of birth is required and must be in the past"));
    }

    @Test
    void testCreateUser_EmptyOrNullFields() {
        Users invalidUser = new Users();
        invalidUser.setName(null);
        invalidUser.setSurname("");
        invalidUser.setNationalId(null);
        invalidUser.setDateOfBirth(LocalDate.parse("2000-02-02"));
        invalidUser.setEmail("");
        invalidUser.setPhoneNumber("09129966331");
        invalidUser.setPassword(null);

        ConstraintViolation<Users> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not be blank");
        when(validator.validate(invalidUser)).thenReturn(Set.of(violation));


        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createUser(invalidUser);
        });

        assertTrue(exception.getMessage().contains("must not be blank"));
    }

    @Test
    void testCreateUser_NonUniqueValues() {
        Users invalidUser = new Users();
        invalidUser.setName("iman");
        invalidUser.setSurname("abc");
        invalidUser.setNationalId("4528422034");
        invalidUser.setDateOfBirth(LocalDate.parse("2000-02-02"));
        invalidUser.setEmail("alreadyused@example.com");
        invalidUser.setPhoneNumber("09129966331");
        invalidUser.setPassword("password123");

        when(usersRepository.findByEmail("alreadyused@example.com"))
                .thenReturn(Optional.of(invalidUser));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createUser(invalidUser);
        });

        assertTrue(exception.getMessage().contains("Email must be unique"));
    }

    @Test
    void testCreateUser_BusinessRuleViolation_MaleWithMilitaryStatusNone() {
        // Create a Users object with invalid military status for a male user
        Users invalidUser = new Users();
        invalidUser.setName("iman");
        invalidUser.setSurname("abc");
        invalidUser.setNationalId("4528422034");
        invalidUser.setDateOfBirth(LocalDate.of(1985, 1, 1));
        invalidUser.setEmail("imanabc@example.com");
        invalidUser.setPhoneNumber("09129966331");
        invalidUser.setPassword("password123");
        invalidUser.setGender(Gender.MALE);
        invalidUser.setMilitaryStatus(MilitaryStatus.NONE);

        ConstraintViolation<Users> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not have military status of 'NONE'");
        when(validator.validate(invalidUser)).thenReturn(Set.of(violation));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createUser(invalidUser);
        });

        assertTrue(exception.getMessage().contains("must not have military status of 'NONE'"));
    }

    @Test
    void testCreateUser_BusinessRuleViolation_FemaleWithInvalidMilitaryStatus() {
        Users invalidUser = new Users();
        invalidUser.setName("Jane");
        invalidUser.setSurname("abc");
        invalidUser.setNationalId("4528422034");
        invalidUser.setDateOfBirth(LocalDate.parse("2000-02-02"));
        invalidUser.setEmail("janeabc@example.com");
        invalidUser.setPhoneNumber("09129966331");
        invalidUser.setPassword("password123");
        invalidUser.setGender(Gender.FEMALE);
        invalidUser.setMilitaryStatus(MilitaryStatus.COMPLETED_SERVICE); // Violation

        ConstraintViolation<Users> violation = mock(ConstraintViolation.class);
        when(violation.getMessage())
                .thenReturn("The military status for female users must be 'NONE'");
        when(validator.validate(invalidUser)).thenReturn(Set.of(violation));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createUser(invalidUser);
        });

        assertTrue(exception.getMessage()
                .contains("The military status for female users must be 'NONE'"));
    }

    // --- Login User Tests ---

    @Test
    void testLoginUser_IncorrectPassword() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");
        user.setPassword("encodedPassword");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.generateToken("imanabc@example.com", "wrongpassword");
        });

        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testLoginUser_IncorrectEmail() {

        when(usersRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> {
            mainService.generateToken("nonexistent@example.com", "password123");
        });

        assertTrue(ex.getMessage().contains("User not found with email"));
    }

    // --- Create Account Tests ---

    @Test
    void testCreateAccount_EmptyAmount() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createAccount("imanabc@example.com", null);
        });

        assertTrue(exception.getMessage().contains("Amount must be a positive number"));
    }

    @Test
    void testCreateAccount_NegativeAmount() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.createAccount("imanabc@example.com", -1000L);
        });

        assertTrue(exception.getMessage().contains("Amount must be a positive number"));
    }

    @Test
    void testCreateAccount_AmountLessThanMinBalance() {
        // Assume minBalance is 10,000
        Long amount = 5000L; // Less than the minimum balance
        String email = "test@example.com";

        Users user = new Users();
        user.setEmail(email);
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Mock the value of minBalance
        ReflectionTestUtils.setField(mainService, "minBalance", 10000L);

        // Expect a ValidationException to be thrown due to the insufficient amount
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            mainService.createAccount(email, amount);
        });

        assertTrue(ex.getMessage().contains("Initial deposit must be greater than"));
    }

    // --- Charge Account Tests ---

    @Test
    void testChargeAccount_EmptyAmount() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.chargeAccount("imanabc@example.com", null);
        });

        assertTrue(exception.getMessage().contains("Amount must be a positive number"));
    }

    @Test
    void testChargeAccount_NegativeAmount() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.chargeAccount("imanabc@example.com", -500L);
        });

        assertTrue(exception.getMessage().contains("Amount must be a positive number"));
    }

    // --- Deduct Amount Tests ---

    @Test
    void testDeductAmount_EmptyAmount() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.deductAmount("imanabc@example.com", null);
        });

        assertTrue(exception.getMessage().contains("Amount must be a positive number"));
    }

    @Test
    void testDeductAmount_NegativeAmount() {
        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.deductAmount("imanabc@example.com", -1000L);
        });

        assertTrue(exception.getMessage().contains("Amount must be a positive number"));
    }

    // --- Update User Info Tests ---

    @Test
    void testUpdateUserInfo_InvalidPhoneNumber() {

        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("iman", "abc", "invalidPhone",
                MilitaryStatus.COMPLETED_SERVICE, null);


        ConstraintViolation<Users> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Phone number must be exactly 11 digits");
        when(validator.validate(user)).thenReturn(Set.of(violation));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.updateUserInfo("imanabc@example.com", updateUserDTO);
        });

        assertTrue(exception.getMessage().contains("Phone number must be exactly 11"));
    }

    @Test
    void testUpdateUserInfo_EmptyFields() {

        Users user = new Users();
        user.setEmail("imanabc@example.com");

        when(usersRepository.findByEmail("imanabc@example.com")).thenReturn(Optional.of(user));

        UpdateUserDTO updateUserDTO =
                new UpdateUserDTO("", "", "09129966331", MilitaryStatus.COMPLETED_SERVICE, null);


        ConstraintViolation<Users> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not be blank");
        when(validator.validate(user)).thenReturn(Set.of(violation));


        ValidationException exception = assertThrows(ValidationException.class, () -> {
            mainService.updateUserInfo("imanabc@example.com", updateUserDTO);
        });

        assertTrue(exception.getMessage().contains("must not be blank"));
    }
}
