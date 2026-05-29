package de.volodymyr.learning.repository;

import de.volodymyr.learning.model.BlogPost;


import java.util.List;


public interface BlogRepository {
    void save(BlogPost post);

    BlogPost delete(int id);

    List<BlogPost> findAll();

    BlogPost find(int id);

    List<BlogPost> wildSearch(String query);
}
