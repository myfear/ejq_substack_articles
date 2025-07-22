package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.langchain4j.model.chat.ChatModel;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

@ApplicationScoped
@Startup
public class TextAnalyticsService {

    // --- Records for Graph Data Structure ---
    public record Node(String id, String label) {
    }

    public record Edge(String source, String target, double sentiment, int weight) {
    }

    public record GraphData(List<Node> nodes, List<Edge> edges) {
    }

    // --- Injected Ollama Model ---
    @Inject
    ChatModel chatModel;

    private String bookText;
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private NameFinderME nameFinder;

    private final Set<String> stopWords = Set.of(
            "a", "an", "and", "the", "in", "on", "of", "to", "is", "it", "i", "you", "he", "she");

    private static final Map<String, List<String>> MAIN_CHARACTERS = Map.of(
            "Huck", List.of("huck", "huckleberry", "Tom Sawyer's Comrade", "Huck Finn", "Sarah Williams", "Sarah Mary Williams George Elexander Peters", "George Peters"),
            "Tom", List.of("tom", "tom sawyer", "Sawyer", "Sawyer's"),
            "Jim", List.of("jim", "Ole Jim", "Watson's Jim", "Watson's", "Mars Tom"),
            "Miss Watson", List.of("miss watson"),
            "Widow Douglas", List.of("widow douglas"),
            "The Duke", List.of("duke"),
            "Harvey Wilks", List.of("king", "Harvey"));

    public TextAnalyticsService() throws IOException {
        Log.info("Loading resources...");
        try (InputStream bookStream = getClass().getResourceAsStream("/huck-finn.txt")) {
            this.bookText = new String(bookStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (InputStream sentModelIn = getClass()
                .getResourceAsStream("/models/opennlp-en-ud-ewt-sentence-1.3-2.5.4.bin");
                InputStream tokenModelIn = getClass()
                        .getResourceAsStream("/models/opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin");
                InputStream personModelIn = getClass().getResourceAsStream("/models/en-ner-person.bin")) {

            this.sentenceDetector = new SentenceDetectorME(new SentenceModel(sentModelIn));
            this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
            this.nameFinder = new NameFinderME(new TokenNameFinderModel(personModelIn));
        }
        Log.info("Resources loaded successfully!");
    }

    // --- Basic Analysis Methods ---
    public long getWordCount() {
        return Arrays.stream(tokenizer.tokenize(this.bookText.toLowerCase())).count();
    }

    public Map<String, Long> getTopWords(int limit) {
        String[] tokens = tokenizer.tokenize(this.bookText.toLowerCase());
        return Arrays.stream(tokens)
                .filter(word -> word.matches("[a-zA-Z]+"))
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public Set<String> findPeople() {
        Set<String> names = new HashSet<>();
        String[] sentences = sentenceDetector.sentDetect(this.bookText);
        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            Span[] nameSpans = nameFinder.find(tokens);
            names.addAll(Arrays.asList(Span.spansToStrings(nameSpans, tokens)));
        }
        nameFinder.clearAdaptiveData();
        return names;
    }

    // --- Character Interaction Graph Methods ---

    public GraphData getInteractionGraph() {
        Map<Set<String>, List<Double>> interactions = new HashMap<>();
        String[] sentences = sentenceDetector.sentDetect(bookText);

        for (String sentence : sentences) {
            Set<String> presentCharacters = findMainCharactersInText(sentence.toLowerCase());
            if (presentCharacters.size() >= 2) {
                double sentimentScore = analyzeSentimentWithLLM(sentence);

                List<String> charList = new ArrayList<>(presentCharacters);
                for (int i = 0; i < charList.size(); i++) {
                    for (int j = i + 1; j < charList.size(); j++) {
                        Set<String> pair = new TreeSet<>(Set.of(charList.get(i), charList.get(j)));
                        interactions.computeIfAbsent(pair, k -> new ArrayList<>()).add(sentimentScore);
                    }
                }
            }
        }
        return buildGraphDataFromInteractions(interactions);
    }

    private Set<String> findMainCharactersInText(String text) {
        return MAIN_CHARACTERS.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(text::contains))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Uses the injected Ollama chat model to analyze sentiment.
     * This method performs "prompt engineering" to get a structured response.
     */
    private double analyzeSentimentWithLLM(String text) {
        String prompt = String.format(
                "You are a sentiment analysis expert. Analyze the following historic text and respond with ONLY a single number "
                        +
                        "representing the sentiment: 2 for very positive, 1 for positive, 0 for neutral, " +
                        "-1 for negative, or -2 for very negative. Do not provide any other text or explanation.  " +
                        "ONLY RESPONSE WITH A SINGLE NUMBER" +
                        "The text is: \"%s\"",
                text);

        try {
            // Send the prompt to the LLM
            String response = chatModel.chat(prompt);
            // The LLM should return just a number. We parse it.
            return Double.parseDouble(response.trim());
        } catch (Exception e) {
            // If the LLM response is not a number or an error occurs, default to neutral.
            Log.errorf("Could not parse sentiment from LLM response: " + e.getMessage());
            return 0.0;
        }
    }

    private GraphData buildGraphDataFromInteractions(Map<Set<String>, List<Double>> interactions) {
        List<Node> nodes = MAIN_CHARACTERS.keySet().stream()
                .map(name -> new Node(name, name))
                .toList();

        List<Edge> edges = interactions.entrySet().stream()
                .map(entry -> {
                    List<String> pair = new ArrayList<>(entry.getKey());
                    double averageSentiment = entry.getValue().stream().mapToDouble(d -> d).average().orElse(0.0);
                    int weight = entry.getValue().size();
                    return new Edge(pair.get(0), pair.get(1), averageSentiment, weight);
                })
                .toList();

        return new GraphData(nodes, edges);
    }
}