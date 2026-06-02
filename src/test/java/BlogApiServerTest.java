import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.Category;
import de.volodymyr.learning.model.Tag;
import de.volodymyr.learning.server.BlogApiServer;
import de.volodymyr.learning.service.BlogService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class BlogApiServerTest {
    @Mock
    private BlogService service;

    private BlogApiServer server;
    private final HttpClient client = HttpClient.newHttpClient();
    @BeforeEach
    void openServer(){
        server = new BlogApiServer(service);
        server.start(8081);
    }
    @AfterEach
    void closeServer(){
        server.stop();
    }


    @Test
    void test_createPost_success() throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Test\",\"content\":\"Short content...\",\"category\":\"Tech\",\"tags\":[\"java\"]}"))
                .header("Content-Type", "application/json")
                .build();
        Mockito.when(service.create(any())).thenReturn(new BlogPost(
                1,
                "Test",
                "Short content...",
                new Category(1, "Tech"),
                List.of(new Tag(1, "java")),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(201, response.statusCode());
    }

    @Test
    void test_GET_Success() throws  IOException, InterruptedException{
        List<BlogPost> listPosts = List.of(
                new BlogPost(
                        1,
                        "Test",
                        "Short content...",
                        new Category(1, "Tech"),
                        List.of(new Tag(1, "java")),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ),
                new BlogPost(
                        2,
                        "Test-2",
                        "Short content...-2",
                        new Category(2, "Revolve"),
                        List.of(new Tag(2, "c#")),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );
        Mockito.when(service.getAllPosts()).thenReturn(listPosts);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNotNull( response.body());

    }

    @Test
    void test_GET_Wild() throws  IOException, InterruptedException{
        BlogPost blogPost = new BlogPost(
                1,
                "Test",
                "Short content...",
                new Category(1, "Tech"),
                List.of(new Tag(1, "java")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Mockito.when(service.searchWild("Test")).thenReturn(List.of(blogPost));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts?query=Test"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void test_ByID_Success() throws  IOException, InterruptedException{
        BlogPost blogPost = new BlogPost(
                1,
                "Test",
                "Short content...",
                new Category(1, "Tech"),
                List.of(new Tag(1, "java")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Mockito.when(service.findById(1)).thenReturn(blogPost);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNotNull(response.body());
    }


    @Test
    void testByID_Unsuccess() throws  IOException, InterruptedException{
        BlogPost blogPost = new BlogPost(
                1,
                "Test",
                "Short content...",
                new Category(1, "Tech"),
                List.of(new Tag(1, "java")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Mockito.when(service.findById(1)).thenThrow(NoSuchElementException.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals("Not Found", response.body());
    }

    @Test
    void testInvalidID() throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts/not-a-number"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals("Bad Request: ID must be a number", response.body());
    }

    @Test
    void testUnknownID() throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/posts/1/subpath"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals("Not Found", response.body());
    }
}
