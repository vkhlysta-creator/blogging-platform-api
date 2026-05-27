package de.volodymyr.learning.repository;

import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.Category;
import de.volodymyr.learning.model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresSQLRepository implements BlogRepository{

    private final Connection connection;

    public PostgresSQLRepository(Connection connection){
        this.connection = connection;
    }

    @Override
    public void save(BlogPost post) {

    }

    @Override
    public BlogPost delete(int id) {
        return null;
    }

    @Override
    public List<BlogPost> findAll() {
        return List.of();
    }

    @Override
    public BlogPost find(int id) {
        BlogPost foundPost = null;
        List<Tag> foundTags = new ArrayList<>();
        String sqlQuery = """
                SELECT\s
                    p.id AS post_id,\s
                    p.title AS post_title,\s
                    p.content AS post_content,\s
                    p.created_at AS post_created_at,\s
                    p.updated_at AS post_updated_at,
                    c.id AS category_id,\s
                    c.name AS category_name,
                    t.id AS tag_id,\s
                    t.name AS tag_name
                FROM posts p
                INNER JOIN categories c ON p.category_id = c.id
                LEFT JOIN post_tags pt ON p.id = pt.post_id
                LEFT JOIN tags t ON pt.tag_id = t.id
                WHERE p.id = ?;""";
        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                if (foundPost == null){
                    Timestamp createdAt = rs.getTimestamp("post_created_at");
                    Timestamp updatedAt = rs.getTimestamp("post_updated_at");
                    Category postCategory = new Category(rs.getInt("category_id"), rs.getString("category_name"));
                    foundPost = new BlogPost(
                            rs.getInt("post_id"),
                            rs.getString("post_title"),
                            rs.getString("post_content"),
                            postCategory,
                            foundTags,
                            createdAt.toLocalDateTime(),
                            updatedAt.toLocalDateTime()
                    );
                }
                int tagID = rs.getInt("tag_id");
                if (tagID != 0){
                    Tag foundTag = new Tag(tagID, rs.getString("tag_name"));
                    foundTags.add(foundTag);
                }

            }
        }catch (SQLException sqle){
            System.out.println("Problem with prepared statement: " + sqle.getMessage());
        }


        return foundPost;
    }
}
