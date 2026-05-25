package de.volodymyr.learning.repository;

import de.volodymyr.learning.model.BlogPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryBlogRepository implements BlogRepository{
    private final AtomicInteger atomarID = new AtomicInteger(1);
    private final Map<Integer, BlogPost> posts = new HashMap<>();

    public Map<Integer, BlogPost> getPosts() {
        return posts;
    }





    @Override
    public void save(BlogPost post) {
        if (post.getId() == 0)
            post.setId(atomarID.getAndIncrement());
        posts.put(post.getId(), post);
    }

    @Override
    public BlogPost delete(int id) {
        return posts.remove(id);
    }

    @Override
    public List<BlogPost> findAll() {
        return new ArrayList<>(posts.values());
    }

    @Override
    public BlogPost find(int id) {
        return posts.get(id);
    }
}
