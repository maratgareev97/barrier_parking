package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.barrier.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
