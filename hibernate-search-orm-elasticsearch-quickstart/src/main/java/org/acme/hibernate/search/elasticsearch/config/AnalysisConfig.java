package org.acme.hibernate.search.elasticsearch.config;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

import io.quarkus.hibernate.search.orm.elasticsearch.SearchExtension;

@SearchExtension 
public class AnalysisConfig implements ElasticsearchAnalysisConfigurer {
    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.analyzer("name").custom() // Define the "name" analyzer
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding");

        context.analyzer("english").custom() // Define (or override) the "english" analyzer
                .tokenizer("standard")
                .tokenFilters("lowercase", "snowball_english", "asciifolding");
                // snowball_english is a stemmer

        context.normalizer("sort").custom() // Define the "sort" normalizer
                .tokenFilters("lowercase", "asciifolding");
    }
}

