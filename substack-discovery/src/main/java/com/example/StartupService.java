package com.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.jboss.logging.Logger;
import org.nd4j.linalg.api.ndarray.INDArray;

import com.example.cluster.Clusterer;
import com.example.cluster.TfIdfExtractor;
import com.example.embed.Embedder;
import com.example.scrape.Scraper;
import com.example.view.ClusterView;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import smile.clustering.KMeans;
import smile.manifold.TSNE;

@ApplicationScoped
public class StartupService {

    private static final Logger LOG = Logger.getLogger(StartupService.class);
    private static final String CACHE_FILE = "target/clusters_cache.ser";

    private List<ClusterView> cachedClusters = new ArrayList<>();
    private boolean isProcessing = false;
    private int processedCount = 0;
    private int totalCount = 0;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Application starting up - checking for cached data...");
        if (loadCachedData()) {
            LOG.info("Loaded cached clusters data. Found " + cachedClusters.size() + " clusters.");
        } else {
            LOG.info("No cached data found - beginning data processing...");
            processDataAsync();
        }
    }

    private void processDataAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                isProcessing = true;
                processData();
            } catch (Exception e) {
                LOG.error("Error during data processing", e);
            } finally {
                isProcessing = false;
            }
        });
    }

    private void processData() throws Exception {
        LOG.info("Loading URLs from data file...");
        List<String> urls = Files.readAllLines(Paths.get("src/main/resources/data/data.txt"));
        totalCount = urls.size();
        LOG.info("Found " + totalCount + " URLs to process");

        // scrape
        LOG.info("Starting web scraping...");
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            LOG.info("Scraping " + (i + 1) + "/" + totalCount + ": " + url);
            texts.add(Scraper.fetch(url));
            processedCount = i + 1;
        }

        LOG.info("Scraping completed. Starting embedding...");
        // embed
        ParagraphVectors vec = Embedder.train(texts);

        LOG.info("Generating feature vectors for " + texts.size() + " articles...");
        // vectors
        double[][] features = new double[texts.size()][];
        for (int i = 0; i < texts.size(); i++) {
            if (i % 10 == 0) {
                LOG.info("Generating vector " + (i + 1) + "/" + texts.size() + " ("
                        + String.format("%.1f", (i * 100.0 / texts.size())) + "%)");
            }
            INDArray v = vec.inferVector(texts.get(i));
            features[i] = v.toDoubleVector();
        }
        LOG.info("Feature vector generation completed!");

        LOG.info("Clustering data...");
        // cluster
        int k = 6;
        KMeans km = Clusterer.cluster(features, k);

        LOG.info("Computing t-SNE coordinates...");
        // tsne
        TSNE tsne = new TSNE(features, 2);
        double[][] coords = tsne.coordinates;

        LOG.info("Building cluster views...");
        // build view
        List<ClusterView> clusters = new ArrayList<>();
        for (int c = 0; c < k; c++) {
            List<String> clusterDocs = new ArrayList<>();
            List<String> clusterUrls = new ArrayList<>();
            List<double[]> clusterCoords = new ArrayList<>();
            for (int i = 0; i < urls.size(); i++) {
                if (km.y[i] == c) {
                    clusterDocs.add(texts.get(i));
                    clusterUrls.add(urls.get(i));
                    clusterCoords.add(coords[i]);
                }
            }
            if (!clusterDocs.isEmpty()) {
                ClusterView cv = new ClusterView();
                cv.clusterId = c;
                cv.keywords = TfIdfExtractor.topTerms(clusterDocs, 8);
                LOG.info("Cluster " + c + " keywords: " + cv.keywords);
                LOG.info("Cluster " + c + " keywords size: " + cv.keywords.size());
                LOG.info("Cluster " + c + " first keyword: " + (cv.keywords.isEmpty() ? "EMPTY" : cv.keywords.get(0)));
                cv.articles = new ArrayList<>();
                for (int i = 0; i < clusterUrls.size(); i++) {
                    ClusterView.ArticleView av = new ClusterView.ArticleView();
                    av.url = clusterUrls.get(i);
                    av.x = clusterCoords.get(i)[0];
                    av.y = clusterCoords.get(i)[1];
                    cv.articles.add(av);
                }
                clusters.add(cv);
            }
        }

        cachedClusters = clusters;
        LOG.info("Data processing completed! Generated " + clusters.size() + " clusters");

        // Save to cache
        saveCachedData();
    }

    public List<ClusterView> getClusters() {
        return cachedClusters;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    private boolean loadCachedData() {
        try {
            if (Files.exists(Paths.get(CACHE_FILE))) {
                // Check if cache is recent (less than 24 hours old)
                long cacheAge = System.currentTimeMillis()
                        - Files.getLastModifiedTime(Paths.get(CACHE_FILE)).toMillis();
                if (cacheAge < 24 * 60 * 60 * 1000) { // 24 hours
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CACHE_FILE))) {
                        @SuppressWarnings("unchecked")
                        List<ClusterView> loaded = (List<ClusterView>) ois.readObject();
                        if (loaded != null && !loaded.isEmpty()) {
                            cachedClusters = loaded;
                            LOG.info("Loaded cached data (age: " + (cacheAge / (60 * 60 * 1000)) + " hours)");
                            return true;
                        }
                    }
                } else {
                    LOG.info("Cache is too old (" + (cacheAge / (60 * 60 * 1000)) + " hours), will reprocess");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.warn("Failed to load cached data: " + e.getMessage());
        }
        return false;
    }

    private void saveCachedData() {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_FILE))) {
                oos.writeObject(cachedClusters);
                LOG.info("Cached clusters data saved to " + CACHE_FILE);
            }
        } catch (IOException e) {
            LOG.error("Failed to save cached data: " + e.getMessage());
        }
    }

}