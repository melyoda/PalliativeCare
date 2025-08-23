package com.projectone.PalliativeCare.repository;



import com.projectone.PalliativeCare.model.Role;
import com.projectone.PalliativeCare.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository marks this interface as a Spring Data repository.
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    List<User> findByRegisteredTopicsContaining(String topicId);
    List<User> findByRole(Role role);
}
