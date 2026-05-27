package de.volodymyr.learning.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogPost {
    private int id;
    private String title;
    private String content;
    private Category category;
    private List<Tag> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BlogPost(int id, String title, String content, Category category, List<Tag> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = new Category(category.id(), category.name());
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BlogPost(int id, String title, String content, Category category, List<Tag> tags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = new Category(category.id(), category.name());
        this.tags = tags;
    }

    public BlogPost(BlogPost post){
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.tags = new ArrayList<>(post.getTags());
        this.createdAt = post.createdAt;
        this.updatedAt = post.updatedAt;
    }

    public int getId() {
        return id;
    }
    public String getTitle(){
        return title;
    }

    public Category getCategory() {
        return category;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getContent() {
        return content;
    }


    public void setCategory(Category category) {
        this.category = category;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

