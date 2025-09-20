package com.example.scrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Scraper {
    public static String fetch(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 Quarkus-Discovery")
                .timeout(20000)
                .get();
        doc.select("script,style,noscript,header,footer,nav").remove();
        return doc.body().text().replaceAll("\\s+", " ").trim();
    }
}