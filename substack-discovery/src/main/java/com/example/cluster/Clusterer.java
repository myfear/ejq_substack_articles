package com.example.cluster;

import smile.clustering.KMeans;

public class Clusterer {
    public static KMeans cluster(double[][] features, int k) {
        return KMeans.fit(features, k);
    }
}