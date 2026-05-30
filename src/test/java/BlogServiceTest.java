import de.volodymyr.learning.model.*;
import de.volodymyr.learning.repository.BlogRepository;
import de.volodymyr.learning.service.BlogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;




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

    @Test
    void savePost_InvalidTitle_ThrowsException(){
        CreatedPost toCreatePostNoTitle = new CreatedPost(
                "",
                "That's the most important thing in our world!...",
                "Creativity",
                List.of("Power", "Brain")
        );
        CreatedPost toCreatePostNull = new CreatedPost(
                null,
                "That's the most important thing in our world!...",
                "Creativity",
                List.of("Power", "Brain")
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> blogService.create(toCreatePostNoTitle));
        Assertions.assertThrows(IllegalArgumentException.class, () -> blogService.create(toCreatePostNull));
        Mockito.verifyNoInteractions(blogRepository);
    }

    @Test
    void testUpdate_Success(){
        BlogPost oldPost = new BlogPost(
                1,
                "Creativity",
                "That's the most important thing in our world!...",
                new Category(1, "Creativity"),
                List.of(new Tag(1, "Power"), new Tag(2, "Brain")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        UpdatedPost toUpdatePost = new UpdatedPost(
                "Creativity",
                "I sad NO!",
                "Creativity",
                List.of("Wow", "Nice")
        );
        Mockito.when(blogRepository.find(1)).thenReturn(oldPost);
        blogService.updatePost(1, toUpdatePost);

        ArgumentCaptor<BlogPost> postCaptor = ArgumentCaptor.forClass(BlogPost.class);
        Mockito.verify(blogRepository).save(postCaptor.capture());

        BlogPost capturedPost = postCaptor.getValue();

        Assertions.assertEquals(1, capturedPost.getId());
        Assertions.assertEquals("Creativity", capturedPost.getTitle());
        Assertions.assertEquals("I sad NO!", capturedPost.getContent());
        Assertions.assertEquals(List.of(new Tag(0, "Wow"), new Tag(0,"Nice")), capturedPost.getTags());

    }

    @Test
    void testUpdate_NoTitle(){
        UpdatedPost toUpdatePost = new UpdatedPost(
                "",
                "I sad NO!",
                "Creativity",
                List.of("Wow", "Nice")
        );
        UpdatedPost toUpdatePost2 = new UpdatedPost(
                null,
                "I sad NO!",
                "Creativity",
                List.of("Wow", "Nice")
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> blogService.updatePost(1, toUpdatePost));
        Assertions.assertThrows(IllegalArgumentException.class, () -> blogService.updatePost(1, toUpdatePost2));
    }

    @Test
    void testUpdate_NoElement(){
        UpdatedPost toUpdatePost = new UpdatedPost(
                "Title",
                "I sad NO!",
                "Creativity",
                List.of("Wow", "Nice")
        );
        Mockito.when(blogRepository.find(1)).thenReturn(null);

        Assertions.assertThrows(NoSuchElementException.class, () -> blogService.updatePost(1, toUpdatePost));
    }

    @Test
    void testDelete_Success(){
        BlogPost oldPost = new BlogPost(
                1,
                "Creativity",
                "That's the most important thing in our world!...",
                new Category(1, "Creativity"),
                List.of(new Tag(1, "Power"), new Tag(2, "Brain")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Mockito.when(blogRepository.delete(1)).thenReturn(oldPost);

        blogService.deletePost(1);
        Mockito.verify(blogRepository).delete(1);
    }

    @Test
    void testDelete_ThrowsException(){
        Mockito.when(blogRepository.delete(1)).thenReturn(null);

        Assertions.assertThrows(NoSuchElementException.class, () -> blogService.deletePost(1));
    }

    @Test
    void testFindAll_Success(){
        BlogPost firstPost = new BlogPost(
                1,
                "Creativity",
                "That's the most important thing in our world!...",
                new Category(1, "Creativity"),
                List.of(new Tag(1, "Power"), new Tag(2, "Brain")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        BlogPost secondPost = new BlogPost(
                2,
                "IT SOLUTIONS ",
                "MAKE CODING GREAT AGAIN!...",
                new Category(1, "IT"),
                List.of(new Tag(3, "IT"), new Tag(4, "Java")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        BlogPost thirdPost = new BlogPost(
                3,
                "All what matters",
                "If somebody thinks...",
                new Category(1, "Philosophy"),
                List.of(new Tag(1, "Brain"), new Tag(5, "Thoughts")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Mockito.when(blogRepository.findAll()).thenReturn(List.of(firstPost, secondPost, thirdPost));

        List<BlogPost> found = blogService.getAllPosts();

        Assertions.assertEquals(1, found.getFirst().getId());
        Assertions.assertEquals(3, found.size());
        Assertions.assertEquals("If somebody thinks...", found.getLast().getContent());
    }

    @Test
    void testWildSearch_Exception(){
        List<BlogPost> found = blogService.searchWild(null);
        Assertions.assertEquals(0, found.size());
    }


}