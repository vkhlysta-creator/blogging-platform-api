package de.volodymyr.learning.service;

import de.volodymyr.learning.model.*;
import de.volodymyr.learning.repository.BlogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

public class BlogService {
    private final BlogRepository repository;


    public BlogService(BlogRepository repository) {
        this.repository = repository;
    }

    public BlogPost create(CreatedPost dto) throws IllegalArgumentException{
        if (dto.title() == null || dto.title().isBlank()){
            throw new IllegalArgumentException("tittle is null or isBlank");
        }
        BlogPost newPost = new BlogPost(0,
                dto.title(),
                dto.content(),
                new Category(0, dto.category()),
                getTags(dto.tags()));
        repository.save(newPost);
        return newPost;
    }

    public BlogPost findById(int id) throws NoSuchElementException{
        BlogPost result = repository.find(id);

        if (result == null){
            throw new NoSuchElementException("Not Found by ID");
        }

        return result;
    }

    public void deletePost(int id){
        BlogPost deletedPost = repository.delete(id);

        if (deletedPost == null){
            throw new NoSuchElementException("Deleted Post doesn't exist");
        }

    }

    public BlogPost updatePost(int id, UpdatedPost dto) throws  IllegalArgumentException, NoSuchElementException{
        if (dto.title() == null || dto.title().isBlank())
            throw new IllegalArgumentException("Tittle is empty");


        BlogPost toUpdatePost = new BlogPost(findById(id));

        toUpdatePost.setTitle(dto.title());
        toUpdatePost.setContent(dto.content());
        toUpdatePost.setUpdatedAt(LocalDateTime.now());
        toUpdatePost.setTags(getTags(dto.tags()));
        toUpdatePost.setCategory(new Category(0, dto.category()));

        repository.save(toUpdatePost);

        return toUpdatePost;

    }


    private List<Tag> getTags(List<String> tag){
        return tag.stream()
                .map(str -> new Tag(0, str))
                .toList();
    }




}
