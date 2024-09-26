package com.imansdev.ackownt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.imansdev.ackownt.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

}
