package com.devnovus.oneBox.domain.user.repository;

import com.devnovus.oneBox.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
