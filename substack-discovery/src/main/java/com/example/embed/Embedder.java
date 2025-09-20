package com.example.embed;

import java.util.List;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.jboss.logging.Logger;

public class Embedder {
    private static final Logger LOG = Logger.getLogger(Embedder.class);

    public static ParagraphVectors train(List<String> docs) {
        LOG.info("Starting ParagraphVectors training with " + docs.size() + " documents...");

        var iterator = new CollectionSentenceIterator(docs);
        var tok = new DefaultTokenizerFactory();

        ParagraphVectors vec = new ParagraphVectors.Builder()
                .iterate(iterator)
                .tokenizerFactory(tok)
                .layerSize(100) // Reduced from 200 to 100 for faster training
                .epochs(5) // Reduced from 10 to 5 epochs
                .minWordFrequency(1) // Reduced from 2 to 1 for more words
                .trainWordVectors(true)
                .build();

        LOG.info("Training ParagraphVectors model...");
        vec.fit();
        LOG.info("ParagraphVectors training completed!");

        return vec;
    }
}