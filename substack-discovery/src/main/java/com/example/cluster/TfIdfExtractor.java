package com.example.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.deeplearning4j.text.stopwords.StopWords;

public class TfIdfExtractor {

    // Additional Java programming and technical qualifiers to filter out (minimal
    // set)
    private static final Set<String> JAVA_TECHNICAL_TERMS = Set.of(
            // Most common Java keywords that don't add semantic value
            "import", "public", "private", "static", "final", "class", "interface", "package", "void",
            "null", "true", "false", "if", "else", "while", "for", "switch", "case", "break", "continue",
            "try", "catch", "finally", "throw", "throws", "new", "this", "super", "return", "instanceof",
            "do", "goto", "const", "default", "byte", "short", "int", "long", "float", "double", "char",
            "boolean", "string", "stringbuilder", "stringbuffer",

            // Common Java packages that don't add semantic value
            "javax", "jakarta", "org", "com", "net", "io", "util", "lang", "awt", "swing", "sql",
            "time", "nio", "security", "crypto", "text", "math", "beans", "rmi", "jdbc", "jpa",
            "jax", "jaxb", "jaxrs", "jaxws", "jms", "jta", "jndi", "jca", "jce", "jdo",

            // HTML/XML tags that don't add semantic value
            "div", "span", "p", "a", "img", "ul", "ol", "li", "table", "tr", "td", "th", "form", "input",
            "button", "select", "option", "textarea", "label", "fieldset", "legend", "h1", "h2", "h3",
            "h4", "h5", "h6", "br", "hr", "meta", "link", "script", "style", "title", "head", "body",

            // Very common technical terms that don't add semantic value
            "value", "data", "result", "config", "settings", "options", "params", "args", "arg", "param",
            "id", "name", "type", "key", "val", "obj", "item", "list", "array", "map", "set",
            "collection", "iterator", "stream", "optional", "nullable", "nonnull");

    public static List<String> topTerms(List<String> docs, int n) {
        Map<String, Integer> df = new HashMap<>();
        List<Map<String, Integer>> termFreqs = new ArrayList<>();

        for (String doc : docs) {
            String[] tokens = doc.toLowerCase().split("\\W+");
            Map<String, Integer> tf = new HashMap<>();
            for (String t : tokens) {
                // Filter out short words, stop words, and Java technical terms
                if (t.length() < 3 || StopWords.getStopWords().contains(t) || JAVA_TECHNICAL_TERMS.contains(t))
                    continue;
                tf.put(t, tf.getOrDefault(t, 0) + 1);
            }
            termFreqs.add(tf);
            for (String t : tf.keySet())
                df.put(t, df.getOrDefault(t, 0) + 1);
        }

        int totalDocs = docs.size();
        Map<String, Double> scores = new HashMap<>();
        for (Map<String, Integer> tf : termFreqs) {
            for (Map.Entry<String, Integer> e : tf.entrySet()) {
                String term = e.getKey();
                double idf = Math.log((1.0 + totalDocs) / (1.0 + df.get(term))) + 1.0;
                double tfidf = e.getValue() * idf;
                scores.put(term, scores.getOrDefault(term, 0.0) + tfidf);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}