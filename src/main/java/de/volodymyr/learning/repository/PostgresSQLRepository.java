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


    private long getOrCreateCategory(String name){
        String querySelect = """
                SELECT id
                FROM categories
                WHERE name = ?;
                """;
        String queryInsert = """
                INSERT INTO categories (name)
                VALUES (?)
                """;
        try (PreparedStatement statementSelect = connection.prepareStatement(querySelect)){
           statementSelect.setString(1, name);

           try(ResultSet selectRS = statementSelect.executeQuery()){
               if (selectRS.next()){
                   return selectRS.getLong("id");
               }
           }

           try (PreparedStatement statementInsert = connection.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS)){
               statementInsert.setString(1, name);
               statementInsert.executeUpdate();

               try (ResultSet generatedID = statementInsert.getGeneratedKeys() ){
                    if (generatedID.next()){
                        return generatedID.getLong(1);
                    }
               }
           }

        }catch (SQLException sqle){
            System.out.println("Exception(Creating Category) : "  + sqle.getMessage());
        }
        return -1;
    }


    private long getOrCreateTag(String name){
        String querySelect = """
                SELECT id
                FROM tags
                WHERE name = ?;
                """;
        String queryInsert = """
                INSERT INTO tags (name)
                VALUES (?)
                """;

        try (PreparedStatement statementSelect = connection.prepareStatement(querySelect)){
            statementSelect.setString(1, name);
            try (ResultSet resultSelect = statementSelect.executeQuery()){
                if (resultSelect.next()){
                    return resultSelect.getLong("id");
                }
            }

            try (PreparedStatement statementInsert = connection.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS)){
                statementInsert.setString(1, name);
                statementInsert.executeUpdate();

                try (ResultSet generatedID = statementInsert.getGeneratedKeys()){
                    if (generatedID.next()){
                        return generatedID.getLong(1);
                    }
                }
            }

        }catch (SQLException sqle){
            System.out.println("Problem with creation of the tag: " + sqle.getMessage());
        }
        return -1;
    }

    private void insertPost(BlogPost post){
        String insertQueryCategories = """
                INSERT INTO posts (title, content, created_at, updated_at, category_id)
                VALUES (?, ?, ?, ?, ?);
                """;
        String insertQueryTags = """
                INSERT INTO post_tags (post_id, tag_id)
                VALUES (?, ?);
                """;
        long categoryID = getOrCreateCategory(post.getCategory().name());

        try (PreparedStatement categoryInsertion = connection.prepareStatement(insertQueryCategories, Statement.RETURN_GENERATED_KEYS)){
            categoryInsertion.setString(1, post.getTitle());
            categoryInsertion.setString(2, post.getContent());
            categoryInsertion.setTimestamp(3, Timestamp.valueOf(post.getCreatedAt()));
            categoryInsertion.setTimestamp(4, Timestamp.valueOf(post.getUpdatedAt()));
            categoryInsertion.setLong(5, categoryID);

            categoryInsertion.executeUpdate();

            try(ResultSet resultInsertionCategory = categoryInsertion.getGeneratedKeys()){
                if (resultInsertionCategory.next()){
                    long post_id = resultInsertionCategory.getLong(1);
                    try (PreparedStatement tagsInsertion = connection.prepareStatement(insertQueryTags)){
                        for (Tag tag : post.getTags()){
                            long tag_id = getOrCreateTag(tag.name());
                            tagsInsertion.setLong(1, post_id);
                            tagsInsertion.setLong(2, tag_id);
                            tagsInsertion.addBatch();
                        }
                        tagsInsertion.executeBatch();
                    }
                }else
                    throw new SQLException("Creating post failed, no ID obtained.");
            }
        }catch (SQLException sqle){
            System.out.println("Problem with insertion: " + sqle.getMessage());
            sqle.printStackTrace();
        }

    }
}
