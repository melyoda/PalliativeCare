package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.Posts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Posts, String> {

    // Find all posts in a topic
    List<Posts> findByTopicId(String topicId);

}