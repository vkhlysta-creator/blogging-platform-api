import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.Category;
import de.volodymyr.learning.model.CreatedPost;
import de.volodymyr.learning.model.Tag;
import de.volodymyr.learning.repository.BlogRepository;
import de.volodymyr.learning.service.BlogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyInt;



@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogService blogService;


    @Test
    void getPostByID_Success(){
        BlogPost expectedPost = new BlogPost(
                1,
                "Creativity",
                "That's the most important thing in our world!",
                new Category(1, "IDEA"),
                List.of(new Tag(1, "Power"), new Tag(2, "Brain")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Mockito.when(blogRepository.find(1)).thenReturn(expectedPost);
        BlogPost found = blogService.findById(1);
        Assertions.assertEquals(1, found.getId());
        Assertions.assertEquals("Creativity", found.getTitle());
        Assertions.assertEquals(List.of(new Tag(1, "Power"), new Tag(2, "Brain")), found.getTags());
    }

    @Test
    void getPostByID_NotFound_ThrowsException(){
        Mockito.when(blogRepository.find(2)).thenReturn(null);
        Assertions.assertThrows(NoSuchElementException.class, () -> blogService.findById(2));
    }

    @Test
    void savePost_Success(){
        CreatedPost toCreatePost = new CreatedPost(
                "Creativity",
                "That's the most important thing in our world!...",
                "Creativity",
                List.of("Power", "Brain")
        );
        BlogPost dummyCreatedPost = new BlogPost(
                1,
                "Creativity",
                "That's the most important thing in our world!...",
                new Category(1, "Creativity"),
                List.of(new Tag(1, "Power"), new Tag(2, "Brain")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Mockito.when(blogRepository.find(Mockito.anyInt())).thenReturn(dummyCreatedPost);

        BlogPost result = blogService.create(toCreatePost);

        ArgumentCaptor<BlogPost> postCaptor = ArgumentCaptor.forClass(BlogPost.class);

        Mockito.verify(blogRepository).save(postCaptor.capture());

        BlogPost capturedPost = postCaptor.getValue();


        Assertions.assertEquals("Creativity", capturedPost.getTitle());
        Assertions.assertEquals("That's the most important thing in our world!...", capturedPost.getContent());
        Assertions.assertEquals(0, capturedPost.getId()); // ID при создании должен быть 0!
        Assertions.assertEquals(1, result.getId());
    }
}