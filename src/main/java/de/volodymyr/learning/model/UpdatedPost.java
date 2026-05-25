package de.volodymyr.learning.model;

import java.util.List;

public record UpdatedPost( String title, String content, String category, List<String> tags) {
}
