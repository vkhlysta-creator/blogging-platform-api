package de.volodymyr.learning.model;

import java.util.List;

public record CreatedPost(String title, String content, String category, List<String> tags) {
}
