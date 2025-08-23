package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
    Optional<Topic> findByTitle(String title);   // search a topic by its exact name

    //List<Topic> findByTitle(String title);

    List<Topic> findByTitleContainingIgnoreCase(String keyword); // search topics by partial name

    Optional<Topic> findByTitleIgnoreCase(String title);

    Optional<Topic> findFirstByTitle(String title);
}
