package de.volodymyr.learning;


import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.CreatedPost;
import de.volodymyr.learning.model.UpdatedPost;
import de.volodymyr.learning.repository.InMemoryBlogRepository;
import de.volodymyr.learning.service.BlogService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        BlogService service = getBlogService();
        service.getAllPosts().forEach(post -> System.out.println(
                "------------------------- Post ------------------------\n" +
                "ID:" + post.getId() + "\n" +
                        "Title: " + post.getTitle() + "\n" +
                        "Content: " + post.getContent() + "\n" +
                        "Category: " + post.getCategory().name() + "\n" +
                        "CreatedAt: " + post.getCreatedAt() + "\n" +
                        "UpdatedAt: " + post.getUpdatedAt() + "\n" +
                        "Tags: " + post.getTags())
                );

        service.updatePost(2, new UpdatedPost(
                "Second universal Post",
                "This post was changed and updated",
                "Death",
                List.of("AI", "Programming")
                ));

        service.getAllPosts().forEach(post -> System.out.println(
                "------------------------- Post ------------------------\n" +
                        "ID:" + post.getId() + "\n" +
                        "Title: " + post.getTitle() + "\n" +
                        "Content: " + post.getContent() + "\n" +
                        "Category: " + post.getCategory().name() + "\n" +
                        "CreatedAt: " + post.getCreatedAt() + "\n" +
                        "UpdatedAt: " + post.getUpdatedAt() + "\n" +
                        "Tags: " + post.getTags()
                ));

        List<BlogPost> postWild = service.searchWild("Death");

        postWild.forEach(blogPost -> System.out.println(
                "---------------------- WILD CARDS SEARCH --------------\n" +
                "------------------------- Post ------------------------\n" +
                        "ID:" + blogPost.getId() + "\n" +
                        "Title: " + blogPost.getTitle() + "\n" +
                        "Content: " + blogPost.getContent() + "\n" +
                        "Category: " + blogPost.getCategory().name() + "\n" +
                        "CreatedAt: " + blogPost.getCreatedAt() + "\n" +
                        "UpdatedAt: " + blogPost.getUpdatedAt() + "\n" +
                        "Tags: " + blogPost.getTags()
        ));

        service.deletePost(2);

        service.getAllPosts().forEach(post -> System.out.println(
                "------------------------- Post ------------------------\n" +
                        "ID:" + post.getId() + "\n" +
                        "Title: " + post.getTitle() + "\n" +
                        "Content: " + post.getContent() + "\n" +
                        "Category: " + post.getCategory().name() + "\n" +
                        "CreatedAt: " + post.getCreatedAt() + "\n" +
                        "UpdatedAt: " + post.getUpdatedAt() + "\n" +
                        "Tags: " + post.getTags()
        ));


    }

    private static BlogService getBlogService() {
        InMemoryBlogRepository repository = new InMemoryBlogRepository();
        BlogService service = new BlogService(repository);
        service.create(new CreatedPost(
                "First universal Post",
                "lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum ",
                "Programming",
                List.of("HR", "Programming", "AI")
        ));
        service.create(new CreatedPost(
                "Second universal Post",
                "lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum ",
                "Life",
                List.of("Work-Life", "Programming", "AI")
        ));
        return service;
    }

}

