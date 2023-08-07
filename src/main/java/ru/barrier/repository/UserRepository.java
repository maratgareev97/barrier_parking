package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.barrier.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


}
