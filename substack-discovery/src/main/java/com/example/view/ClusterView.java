package com.example.view;

import java.io.Serializable;
import java.util.List;

public class ClusterView implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int clusterId;
    public List<String> keywords;
    public List<ArticleView> articles;

    public static class ArticleView implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public String url;
        public double x;
        public double y;
    }
}