package de.volodymyr.learning.service;

import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.Category;
import de.volodymyr.learning.model.CreatedPost;
import de.volodymyr.learning.model.Tag;
import de.volodymyr.learning.repository.BlogRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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


    private List<Tag> getTags(List<String> tag){
        return tag.stream()
                .map(str -> new Tag(0, str))
                .toList();
    }




}
