package com.example.github;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AwesomePromptsClient {

    private static final Logger LOG = Logger.getLogger(AwesomePromptsClient.class);
    private static final List<String> TARGET_DIRECTORIES = List.of("prompts", "instructions");

    @ConfigProperty(name = "mcp.repository.owner")
    String owner;
    @ConfigProperty(name = "mcp.repository.name")
    String repo;
    @ConfigProperty(name = "mcp.repository.branch", defaultValue = "main")
    String branch;

    public List<GHContent> fetchPromptFiles() throws IOException {
        LOG.info("Fetching prompt files from repository: " + owner + "/" + repo);
        GitHub gh = getGitHubClient();
        GHRepository repository = gh.getRepository(owner + "/" + repo);

        List<GHContent> allMarkdownFiles = new ArrayList<>();

        for (String targetDir : TARGET_DIRECTORIES) {
            try {
                LOG.info("Processing directory: /" + targetDir);
                List<GHContent> contents = List.copyOf(repository.getDirectoryContent(targetDir, branch));
                List<GHContent> markdownFiles = listMarkdownFiles(contents);
                allMarkdownFiles.addAll(markdownFiles);
                LOG.info("Found " + markdownFiles.size() + " markdown files in /" + targetDir);
            } catch (IOException e) {
                LOG.warn("Directory /" + targetDir + " not found or inaccessible: " + e.getMessage());
            }
        }

        LOG.info("Total markdown files found: " + allMarkdownFiles.size());
        return allMarkdownFiles;
    }

    public String fetchFileContent(String path) throws IOException {
        LOG.info("Fetching file content for: " + path);
        GitHub gh = getGitHubClient();
        try (InputStream is = gh.getRepository(owner + "/" + repo)
                .getFileContent(path, branch)
                .read()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            LOG.info("Successfully fetched " + content.length() + " characters from " + path);
            return content;
        }
    }

    private GitHub getGitHubClient() throws IOException {
        return GitHub.connect();
    }

    private List<GHContent> listMarkdownFiles(List<GHContent> contents) throws IOException {
        return contents.stream()
                .<GHContent>flatMap(c -> {
                    try {
                        if (c.isDirectory()) {
                            // Recursively process subdirectories
                            return listMarkdownFiles(c.listDirectoryContent().toList()).stream();
                        }
                        if (c.getName().endsWith(".md")) {
                            return Stream.of(c);
                        }
                    } catch (IOException ignored) {
                    }
                    return Stream.empty();
                })
                .toList();
    }
}