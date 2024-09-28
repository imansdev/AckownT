package com.imansdev.ackownt.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.imansdev.ackownt.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByPhoneNumber(String phoneNumber);

    Optional<Users> findByNationalId(String nationalId);

}
