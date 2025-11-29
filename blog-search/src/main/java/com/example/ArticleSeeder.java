package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ArticleSeeder {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    @Inject
    ArticleRepository repo;

    void onStart(@Observes StartupEvent ev) {
        seedArticles();
    }

    @Transactional
    void seedArticles() {
        try {
            URL postsUrl = getClass().getClassLoader().getResource("posts");
            if (postsUrl == null) {
                System.err.println("Posts directory not found in resources");
                return;
            }

            Path postsPath = null;
            FileSystem fileSystem = null;

            try {
                URI uri = postsUrl.toURI();
                
                if ("file".equals(uri.getScheme())) {
                    // Running from filesystem (dev mode)
                    postsPath = Paths.get(uri);
                } else if ("jar".equals(uri.getScheme())) {
                    // Running from JAR
                    try {
                        fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                        postsPath = fileSystem.getPath("posts");
                    } catch (IOException e) {
                        // FileSystem might already be open, try to get existing one
                        fileSystem = FileSystems.getFileSystem(uri);
                        postsPath = fileSystem.getPath("posts");
                    }
                } else {
                    System.err.println("Unsupported resource URI scheme: " + uri.getScheme());
                    return;
                }

                if (postsPath == null || !Files.exists(postsPath)) {
                    System.err.println("Posts path does not exist");
                    return;
                }

                AtomicInteger processedCount = new AtomicInteger(0);
                try (Stream<Path> paths = Files.walk(postsPath)) {
                    paths.filter(Files::isRegularFile)
                         .filter(p -> p.toString().endsWith(".html"))
                         .forEach(htmlPath -> {
                             try {
                                String filename = htmlPath.getFileName().toString();
                                String htmlContent = Files.readString(htmlPath, StandardCharsets.UTF_8);
                                String title = extractTitleFromFilename(filename);
                                String url = extractUrlFromFilename(filename);
                                String plainText = stripHtmlTags(htmlContent);
                                
                                Article article = new Article();
                                article.title = title;
                                article.url = url;
                                article.content = plainText;
                                article.createdAt = Instant.now();
                                 
                                 repo.insert(article);
                                 processedCount.incrementAndGet();
                             } catch (IOException e) {
                                 System.err.println("Error processing file: " + htmlPath + " - " + e.getMessage());
                             }
                         });
                }
                
                System.out.println("Processed " + processedCount.get() + " articles");
            } finally {
                if (fileSystem != null) {
                    fileSystem.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing article seeder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractTitleFromFilename(String filename) {
        // Remove .html extension
        String withoutExt = filename.replace(".html", "");
        // Remove leading number and dot (e.g., "171437814.")
        String title = withoutExt.replaceFirst("^\\d+\\.", "");
        // Replace hyphens with spaces and capitalize words
        String[] words = title.split("-");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1));
                }
            }
        }
        return result.toString();
    }

    private String extractUrlFromFilename(String filename) {
        // Remove .html extension
        String withoutExt = filename.replace(".html", "");
        // Remove leading number and dot (e.g., "171437814.")
        String slug = withoutExt.replaceFirst("^\\d+\\.", "");
        // Construct URL: https://www.the-main-thread.com/p/{slug}
        return "https://www.the-main-thread.com/p/" + slug;
    }

    private String stripHtmlTags(String html) {
        // Remove HTML tags
        String text = HTML_TAG_PATTERN.matcher(html).replaceAll(" ");
        // Decode HTML entities (basic ones)
        text = text.replace("&nbsp;", " ")
                   .replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'");
        // Replace multiple spaces with single space
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        // Trim and return
        return text.trim();
    }
}
