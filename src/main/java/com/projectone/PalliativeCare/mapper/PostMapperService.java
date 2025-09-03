package com.projectone.PalliativeCare.mapper;

import com.projectone.PalliativeCare.dto.*;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.model.Posts;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostMapperService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    public EnrichedPostDTO toEnrichedPostDTO(Posts post) {
        // Fetch author information
        User author = userRepository.findById(post.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + post.getCreatedBy()));

        // Fetch topic information
        Topic topic = topicRepository.findById(post.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + post.getTopicId()));

        // Map comments to enriched DTOs
        List<CommentResponseDTO> enrichedComments = post.getComments().stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());

        return EnrichedPostDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .creationDate(post.getCreationDate())
                .modificationDate(post.getModificationDate())
                .author(toAuthorDTO(author))
                .topic(toTopicInfoDTO(topic))
                .resources(post.getResources())
                .comments(enrichedComments)
                .commentCount(post.getComments().size())
                .build();
    }

    private AuthorDTO toAuthorDTO(User user) {
        return AuthorDTO.builder()
                .authorId(user.getId())
                .authorFirstName(user.getFirstName())
                .authorLastName(user.getLastName())
                .authorEmail(user.getEmail())
                .authorRole(user.getRole())
                .build();
    }

    private TopicInfoDTO toTopicInfoDTO(Topic topic) {
        return TopicInfoDTO.builder()
                .topicId(topic.getId())
                .topicName(topic.getTitle())
                .topicDescription(topic.getDescription())
                .topicLogoUrl(topic.getLogoUrl())
                .build();
    }

    private CommentResponseDTO toCommentDTO(Posts.Comment comment) {
        User commentAuthor = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + comment.getUserId()));

        return CommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .text(comment.getText())
                .timestamp(comment.getTimestamp())
                .lastEdited(comment.getLastEdited())
                .author(toAuthorDTO(commentAuthor))
                .build();
    }

    // Batch mapping for lists
    public List<EnrichedPostDTO> toEnrichedPostDTOList(List<Posts> posts) {
        return posts.stream()
                .map(this::toEnrichedPostDTO)
                .collect(Collectors.toList());
    }

    public Page<EnrichedPostDTO> toEnrichedPostDTOPage(Page<Posts> postsPage) {
        List<EnrichedPostDTO> dtos = toEnrichedPostDTOList(postsPage.getContent());
        return new PageImpl<>(dtos, postsPage.getPageable(), postsPage.getTotalElements());
    }
}
