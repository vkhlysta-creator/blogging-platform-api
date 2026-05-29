import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.Category;
import de.volodymyr.learning.model.Tag;
import de.volodymyr.learning.repository.PostgresSQLRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PostgresSQLRepositoryTest {
    private static Connection connection;

    @BeforeAll
    static void openConnection(){
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost/blog_platform_test", "my_user", "my_password");
        } catch (SQLException e) {
            System.out.println("exception " + e.getMessage());
        }

    }

    @BeforeEach
    void clearTablesAndInsert(){
        String truncateQuery = """
                TRUNCATE TABLE posts, categories, tags, post_tags RESTART IDENTITY CASCADE;
                """;
        String insertCategories = """
                INSERT INTO categories (name) VALUES
                  ('Technology'),
                  ('Science'),
                  ('Health'),
                  ('Finance'),
                  ('Travel');
                """;
        String insertQueryPosts = """ 
                INSERT INTO posts (title, content, created_at, updated_at, category_id) VALUES
                  ('Getting Started with PostgreSQL', 'PostgreSQL is a powerful open-source relational database...', NOW(), NOW(), 1),
                  ('The Future of Quantum Computing', 'Quantum computers leverage superposition and entanglement...', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 2),
                  ('10 Tips for Better Sleep', 'Sleep is essential for physical and mental health...', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', 3);
                """;
        String insertQueryLinks = """
                INSERT INTO post_tags (post_id, tag_id) VALUES
                  (1, 1),
                  (1, 3),
                  (1, 7),
                  (2, 2),
                  (2, 5),
                  (3, 6),
                  (3, 1);
                """;
        String insertQueryTags = """
                INSERT INTO tags (name) VALUES
                  ('beginner'),
                  ('advanced'),
                  ('tutorial'),
                  ('opinion'),
                  ('research'),
                  ('tips'),
                  ('open-source'),
                  ('budgeting')
                """;

        try (
                PreparedStatement psTruncate = connection.prepareStatement(truncateQuery);
                PreparedStatement psPost = connection.prepareStatement(insertQueryPosts);
                PreparedStatement psTags = connection.prepareStatement(insertQueryTags);
                PreparedStatement psPostTags = connection.prepareStatement(insertQueryLinks);
                PreparedStatement psCategories = connection.prepareStatement(insertCategories)
        ){
            psTruncate.executeUpdate();
            psCategories.executeUpdate();
            psPost.executeUpdate();
            psTags.executeUpdate();
            psPostTags.executeUpdate();
        }catch (SQLException sqle){
            System.out.println("Exception: " + sqle.getMessage());
        }

    }

    @AfterAll
    static void closeConnection(){
        try {
            if (connection != null){
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());;
        }
    }

    @Test
    void testFindByID(){
        PostgresSQLRepository repository = new PostgresSQLRepository(connection);
        BlogPost blogPost = repository.find(1);
        Assertions.assertNotNull(blogPost);
        Assertions.assertEquals(1 ,blogPost.getId());
        Assertions.assertEquals("Getting Started with PostgreSQL", blogPost.getTitle());
        Assertions.assertEquals(3, blogPost.getTags().size());
    }

    @Test
    void testFindALl(){
        PostgresSQLRepository repository = new PostgresSQLRepository(connection);
        List<BlogPost> postList = repository.findAll();
        Assertions.assertNotNull(postList);
        Assertions.assertEquals(3, postList.size());
        Assertions.assertEquals(1, postList.getFirst().getId());
        Assertions.assertEquals(3, postList.getLast().getId());
        Assertions.assertEquals("Getting Started with PostgreSQL", postList.getFirst().getTitle());
        Assertions.assertNull(repository.find(99));
    }

    @Test
    void testSave(){
        PostgresSQLRepository repository = new PostgresSQLRepository(connection);
        BlogPost postCreated = new BlogPost(0, "Great test", "I love testing", new Category(1, "Technology" ), List.of(new Tag(1, "beginner"), new Tag(3,"tutorial" )), LocalDateTime.now(), LocalDateTime.now());
        try {
            repository.save(postCreated);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception");;
        }
        List<BlogPost> posts = repository.findAll();
        Assertions.assertEquals(4, posts.size());
        List<BlogPost> postsWithGreatTitle = posts.stream()
                .filter(post -> post.getTitle().equals("Great test"))
                .toList();
        BlogPost savedPost = postsWithGreatTitle.getFirst();
        Assertions.assertEquals("Great test", savedPost.getTitle());
        Assertions.assertTrue(savedPost.getId() > 0);
        Assertions.assertEquals(2, savedPost.getTags().size());
    }

    @Test
    void testDelete(){
        PostgresSQLRepository repository = new PostgresSQLRepository(connection);
        BlogPost deletedPost = repository.delete(2);
        BlogPost post = repository.find(2);
        Assertions.assertNull(post);
    }

    @Test
    void testUpdate(){
        PostgresSQLRepository repository = new PostgresSQLRepository(connection);
        BlogPost toUpdatePost = repository.find(1);

        toUpdatePost.setTitle("Updated!");

        repository.save(toUpdatePost);

        Assertions.assertEquals("Updated!", repository.find(1).getTitle());
    }

    @Test
    void  testWildSearch(){

    }
}
