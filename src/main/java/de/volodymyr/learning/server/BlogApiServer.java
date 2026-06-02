package de.volodymyr.learning.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.CreatedPost;
import de.volodymyr.learning.model.UpdatedPost;
import de.volodymyr.learning.service.BlogService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

public class BlogApiServer {
    private HttpServer server;
    private final BlogService service;
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

    public BlogApiServer(BlogService service) {
        this.service = service;
    }

    public void start(int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/posts", new PostHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            System.out.println("Exception creation of the Server: " + e.getMessage());
        }
    }

    public void stop() {
        server.stop(0);


    }

    private class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            switch (method) {

                case "GET" -> {
                    String query = exchange.getRequestURI().getQuery();
                    String path = exchange.getRequestURI().getPath();

                    if (query != null && query.contains("query=")) {
                        String[] querySplited = query.split("query=");

                        if (querySplited.length > 1) {
                            List<BlogPost> wildPosts = service.searchWild(querySplited[1]);
                            sendConfirmation(exchange, mapper.writeValueAsString(wildPosts), 200);

                        } else {
                            sendConfirmation(exchange, "[]", 200);
                        }
                        return;

                    }

                    String[] pathParts = path.split("/");


                    if (pathParts.length >= 3 && "api".equals(pathParts[1]) && "posts".equals(pathParts[2])) {

                        if (pathParts.length == 3) {
                            List<BlogPost> allPosts = service.getAllPosts();
                            sendConfirmation(exchange, mapper.writeValueAsString(allPosts), 200);
                            return;
                        }

                        if (pathParts.length == 4) {
                            try {
                                int parsedID = Integer.parseInt(pathParts[3]);
                                sendConfirmation(exchange, mapper.writeValueAsString(service.findById(parsedID)), 200);
                            } catch (NumberFormatException e) {
                                System.out.println("Exception: " + e.getMessage());
                                sendConfirmation(exchange, "Bad Request: ID must be a number", 400);
                            } catch (NoSuchElementException ne) {
                                sendConfirmation(exchange, "Not Found", 404);
                            }
                            return;
                        }

                    }
                    sendConfirmation(exchange, "Not Found", 404);
                }

                case "POST" -> {
                    byte[] bytes = exchange.getRequestBody().readAllBytes();
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    CreatedPost dto = mapper.readValue(body, CreatedPost.class);
                    BlogPost savedPost = service.create(dto);
                    sendConfirmation(exchange, mapper.writeValueAsString(savedPost), 201);
                }

                case "PUT" -> {
                    String path = exchange.getRequestURI().getPath();
                    String[] pathParts = path.split("/");
                    if (pathParts.length == 4) {
                        try {
                            int id = Integer.parseInt(pathParts[3]);
                            byte[] bytes = exchange.getRequestBody().readAllBytes();
                            String body = new String(bytes, StandardCharsets.UTF_8);
                            UpdatedPost dto = mapper.readValue(body, UpdatedPost.class);
                            try {
                                service.updatePost(id, dto);
                                sendConfirmation(exchange, "Post updated", 200);
                            } catch (NoSuchElementException e) {
                                sendConfirmation(exchange, "Not Found", 404);
                            } catch (IllegalArgumentException e) {
                                sendConfirmation(exchange, "Bad Request: Titel must be present", 400);
                            }

                        } catch (NumberFormatException e) {
                            sendConfirmation(exchange, "Bad Request: ID must be a number", 400);
                        }
                    } else
                        sendConfirmation(exchange, "Not Found", 404);
                }

                case "DELETE" -> {
                    String path = exchange.getRequestURI().getPath();
                    String[] pathParts = path.split("/");
                    if (pathParts.length == 4) {

                        try {
                            int id = Integer.parseInt(pathParts[3]);
                            try {
                                service.deletePost(id);
                                sendConfirmation(exchange, "OK", 200);

                            } catch (NoSuchElementException ne) {
                                sendConfirmation(exchange, "Not Found", 404);
                            }
                        } catch (NumberFormatException e) {

                            sendConfirmation(exchange, "Bad Request: ID must be a number", 400);

                        }
                    } else
                        sendConfirmation(exchange, "Not Found", 404);

                }

                default -> sendConfirmation(exchange, "405 Method not allowed here! ", 405);

            }

        }

        private static void sendConfirmation(HttpExchange exchange, String output, int statusCode) throws IOException {
            exchange.sendResponseHeaders(statusCode, output.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(output.getBytes());
            }
        }
    }
}
