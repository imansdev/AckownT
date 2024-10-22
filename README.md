# AckownT

**AckownT** is a backend API that allows users to create and manage accounts, conduct financial transactions (including charges and deductions), and securely authenticate using JWT (JSON Web Token) for authorization. The system includes comprehensive validation mechanisms for user data, military status, and enforces business rules on transaction limits and initial deposit amounts.

This backend service provides RESTful endpoints and is designed to ensure data integrity, secure authentication, and robust transaction management.

<td align="center">
  <img width="1107" alt="Screenshot 2024-10-20 at 7 19 33 PM" src="https://github.com/user-attachments/assets/c3e472de-477a-4ffa-850a-dc3b1bbc8e82">
</td>
    
## Features

### User Management
- Create new users with unique email, phone number, and national ID.
- Update user information such as name, surname, phone number, military status, and password.
- Delete users and their related transaction data.

### Account Management
- Create new accounts for registered users with an account opening amount.

### Transaction Management
- Charge accounts with specific amounts.
- Deduct amounts from accounts with daily limits and valid range amounts.
- Track user transactions.

### Authentication
- Authenticate users using email and password.
- Generate JWT tokens for secure API access.

### Validation
- Validate user data using custom annotations (e.g., military status validation, Iranian national ID validation).
- Ensure unique fields (email, phone number, national ID).
- Enforce minimum balance and transaction limits.
- Enforce data input JSON parsing to validate incoming data in the correct formats.
- Perform strict validation on transaction and account amounts.
- Enforce business rules based on gender and military status.

## Technologies Used

### Back-end
- **Spring Boot**
- **Spring Security**
- **Spring Data JPA**
- **Spring MVC**
- **Spring Validation**
  -  Validation framework using Jakarta Bean Validation annotations for object-level validation.

### Database
- **H2 Database**

### Security
- **JWT**: Token-based authentication
- **BCrypt**: Password hashing

### Build Tool
- **Maven**

### Version Control
- **Git**

## API Documentation

### Base URL
All API endpoints use the following base URL: `/api/v1`

> **Note:** Authentication is required for most of the endpoints. You must first log in to get a JWT token, which should be included in all subsequent requests in the Authorization header as a Bearer token.

---

### 1. Create User

#### Endpoint
`/home/create`

#### Method
`POST`

#### Description
Creates a new user.

#### Sample Request Body
```json
{
  "name": "iman",
  "surname": "abc",
  "nationalId": "4528422034",
  "dateOfBirth": "2000-02-02",
  "email": "imanabc@example.com",
  "phoneNumber": "09129966331",
  "password": "password123",
  "gender": "MALE",
  "militaryStatus": "COMPLETED_SERVICE"
}

```

#### Sample Response

```json
{
  "name": "iman",
  "surname": "abc",
  "nationalId": "4528422034",
  "dateOfBirth": "2000-02-02",
  "email": "imanabc@example.com",
  "phoneNumber": "09129966331",
  "gender": "MALE",
  "militaryStatus": "COMPLETED_SERVICE"
}

```

* * * * *

### 2\. Login User

#### Endpoint

`/home/login`

#### Method

`POST`

#### Description

Authenticates a user and returns a JWT token.

#### Sample Request Body


```json
{
  "email": "imanabc@example.com",
  "password": "password123"
}

```

#### Sample Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR..."
}

```

* * * * *

### 3\. Create Account

#### Endpoint

`/account/create`

#### Method

`POST`

#### Description

Creates a new account for the authenticated user with an initial deposit.

#### Request Parameters

-   `amount`: Initial deposit amount.

#### Sample Response

```json
{
  "transactionName": "CHARGE",
  "transactionStatus": "SUCCESSFUL",
  "amount": 9000000,
  "trackingNumber": "777560",
  "transactionDate": "2024-10-20",
  "description": "CHARGING_SUCCESSFUL",
  "withdrawalBalance": 8990000
}

```

* * * * *

### 4. Charge Account

#### Endpoint
`/account/transaction/charge`

#### Method
`POST`

#### Description
Adds a specified amount to the user's account balance.

#### Request Parameters
- `amount`: Amount to charge.

#### Sample Response
```json
{
  "transactionName": "CHARGE",
  "transactionStatus": "SUCCESSFUL",
  "amount": 1000000,
  "trackingNumber": "328315",
  "transactionDate": "2024-10-20",
  "description": "CHARGING_SUCCESSFUL",
  "withdrawalBalance": 9990000
}

```

* * * * *

### 5\. Deduct Amount from Account

#### Endpoint

`/account/transaction/deduction`

#### Method

`POST`

#### Description

Deducts a specified amount from the user's account balance.

#### Request Parameters

-   `amount`: Amount to deduct.

#### Sample Response

```json
{
  "transactionName": "DEDUCTION",
  "transactionStatus": "SUCCESSFUL",
  "amount": 5000000,
  "trackingNumber": "177776",
  "transactionDate": "2024-10-20",
  "description": "DEDUCTION_SUCCESSFUL",
  "withdrawalBalance": 4990000
}

```

* * * * *

### 6\. List User Transactions

#### Endpoint

`/account/transaction/list`

#### Method

`GET`

#### Description

Retrieves the user's account and transaction history.

#### Sample Response

```json
{
  "transactions": [
    {
      "transactionName": "CHARGE",
      "transactionStatus": "SUCCESSFUL",
      "amount": 9000000,
      "trackingNumber": "777560",
      "transactionDate": "2024-10-20",
      "description": "CHARGING_SUCCESSFUL",
      "withdrawalBalance": 8990000
    },
    {
      "transactionName": "CHARGE",
      "transactionStatus": "SUCCESSFUL",
      "amount": 1000000,
      "trackingNumber": "328315",
      "transactionDate": "2024-10-20",
      "description": "CHARGING_SUCCESSFUL",
      "withdrawalBalance": 9990000
    },
    {
      "transactionName": "DEDUCTION",
      "transactionStatus": "SUCCESSFUL",
      "amount": 5000000,
      "trackingNumber": "177776",
      "transactionDate": "2024-10-20",
      "description": "DEDUCTION_SUCCESSFUL",
      "withdrawalBalance": 4990000
    }
  ],
  "account": {
    "accountNumber": "1667359668",
    "balance": 5000000,
    "accountCreationDate": "2024-10-20"
  }
}

```

* * * * *

### 7\. Get User Information

#### Endpoint

`/account`

#### Method

`GET`

#### Description

Retrieves the authenticated user's information.

#### Sample Response

```json
{
  "name": "iman",
  "surname": "abc",
  "nationalId": "4528422034",
  "dateOfBirth": "2000-02-02",
  "email": "imanabc@example.com",
  "phoneNumber": "09129966331",
  "gender": "MALE",
  "militaryStatus": "COMPLETED_SERVICE"
}

```

* * * * *

### 8\. Update User Information

#### Endpoint

`/account/update`

#### Method

`PUT`

#### Description

Updates the user's information.

#### Sample Request Body

```json
{
  "name": "newName",
  "surname": "newSurname",
  "phoneNumber": "09129966332",
  "militaryStatus": "EXEMPT_FROM_SERVICE",
  "password": "newSecurePassword123"
}

```

#### Sample Response

```json
{
  "name": "newName",
  "surname": "newSurname",
  "nationalId": "4528422034",
  "dateOfBirth": "2000-02-02",
  "email": "imanabc@example.com",
  "phoneNumber": "09129966332",
  "gender": "MALE",
  "militaryStatus": "EXEMPT_FROM_SERVICE"
}

```


* * * * *

### 9\. Delete User Account

#### Endpoint

`/account/delete`

#### Method

`DELETE`

#### Description

Deletes the user's account and all related data.

#### Sample Response

```json
{
  "message": "Account and related data deleted successfully"
}

```

## Usage

Follow these steps to run the project:

### 1. Clone the repository
```bash
git clone https://github.com/imansdev/AckownT.git

```

### 2\. Run the H2 Database

H2 is an in-memory database used for this project. Spring Boot will automatically configure and start the H2 database when the application is run.
> **Note:** The H2 database JAR file is located in the root project directory and is named h2-2.2.224.jar.

```bash
java -cp /path/to/h2-2.2.224.jar org.h2.tools.Server -ifNotExists -webAllowOthers -tcpAllowOthers
```

### 3\. Run the Spring Boot project

Use the following command to start the Spring Boot application:


```bash
mvn spring-boot:run

```

> **Alternatively** if you're using an IDE like IntelliJ or Eclipse, you can run the `main` method in the `AckowntApplication.java` file.

### 4\. Use Postman to test the APIs

1.  Open [Postman](https://www.postman.com/).
2.  Import the `AckownT.postman_collection.json` file from the project folder into Postman.
3.  Use the pre-configured URLs in the Postman collection to test the available APIs.