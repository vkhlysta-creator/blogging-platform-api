package de.volodymyr.learning.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.volodymyr.learning.model.BlogPost;
import de.volodymyr.learning.model.CreatedPost;
import de.volodymyr.learning.service.BlogService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BlogApiServer {
    private HttpServer server;
    private final BlogService service;
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

    public BlogApiServer(BlogService service){
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

    public void stop(){
        server.stop(0);


    }

    private class PostHandler implements HttpHandler {
        @Override
        public void handle (HttpExchange exchange) throws IOException{
            String method = exchange.getRequestMethod();
            switch (method){

                case "GET" -> {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null){
                        String[] querySplited = query.split("query=");
                    }
                    List<BlogPost> allPosts =  service.getAllPosts();
                    String json = mapper.writeValueAsString(allPosts);
                    sendConfirmation(exchange, json, 200);
                }

                case "POST" -> {
                    byte[] bytes = exchange.getRequestBody().readAllBytes();
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    CreatedPost dto = mapper.readValue(body, CreatedPost.class);
                    BlogPost savedPost = service.create(dto);
                    sendConfirmation(exchange, mapper.writeValueAsString(savedPost), 201);
                }

                case "PUT" -> sendConfirmation(exchange, "200 PUT", 200);

                case "DELETE" -> sendConfirmation(exchange, "200 DELETE", 200);

                default -> sendConfirmation(exchange, "405 Method not allowed here! ", 405);

            }

        }

        private static void sendConfirmation(HttpExchange exchange, String output, int statusCode) throws IOException {
            exchange.sendResponseHeaders(statusCode, output.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()){
                os.write(output.getBytes());
            }
        }
    }
}
