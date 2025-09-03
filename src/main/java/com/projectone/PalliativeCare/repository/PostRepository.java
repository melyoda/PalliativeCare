package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Posts, String> {

    // Find all posts in a topic
    List<Posts> findByTopicId(String topicId);

    List<Posts> findByCreatedBy(String createdBy);

    List<Posts> findByTopicIdInOrderByCreationDateDesc(List<String> topicIds);

    List<Posts> findByCreatedByAndTopicId(String createdBy, String topicId);

    // Find posts by multiple topic IDs, ordered by date (newest first)
//    List<Posts> findByTopicIdInOrderByCreationDateDesc(List<String> topicIds);

    List<Posts> findByTopicIdInAndCreationDateAfterOrderByCreationDateDesc(
            List<String> topicIds, LocalDateTime date);

    // Paginated version
    Page<Posts> findByTopicIdInOrderByCreationDateDesc(List<String> topicIds, Pageable pageable);

    // Optional: If you want to limit the number of posts
    List<Posts> findTop20ByTopicIdInOrderByCreationDateDesc(List<String> topicIds);

    Page<Posts> findByTopicIdOrderByCreationDateDesc(String topicId,Pageable pageable);
}