package com.rest1.domain.post.post.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.post.comment.entity.Comment;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post write(Member author, String title, String content) {
        Post post = new Post(author, title, content);

        return postRepository.save(post);
    }

    public long count() {
        return postRepository.count();
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public void modify(Post post, String title, String content) {
        post.update(title, content);
    }

    public Comment writeComment(Member author, Post post, String content) {
        return post.addComment(author, content);
    }

    public void deleteComment(Post post, Long commentId) {
        post.deleteComment(commentId);
    }

    public void modifyComment(Post post, Long commentId, String content) {
        post.updateComment(commentId, content);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public void flush() {
        postRepository.flush();
    }
}
