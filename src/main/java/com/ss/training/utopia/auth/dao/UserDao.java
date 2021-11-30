package com.ss.training.utopia.auth.dao;

import com.ss.training.utopia.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByEmailOrUsernameOrPhone(String email, String username, String phone);
}
