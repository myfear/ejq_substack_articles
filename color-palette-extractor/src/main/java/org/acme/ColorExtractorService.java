package org.acme;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ColorExtractorService {

    public List<String> extractColors(InputStream inputStream, int numColors) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);
        int[][] pixels = getPixelArray(image);

        List<int[]> centroids = initializeCentroids(pixels, numColors);
        List<List<int[]>> clusters;

        // Iterate a few times for the K-Means algorithm to converge
        for (int i = 0; i < 10; i++) {
            clusters = assignToClusters(pixels, centroids);
            centroids = updateCentroids(clusters);
        }

        List<String> hexColors = new ArrayList<>();
        for (int[] centroid : centroids) {
            hexColors.add(String.format("#%02x%02x%02x", centroid[0], centroid[1], centroid[2]));
        }
        return hexColors;
    }

    private int[][] getPixelArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        // For performance, we can sample the pixels instead of using all of them
        int sampleRate = Math.max(1, (width * height) / 10000); // Sample ~10,000 pixels
        List<int[]> pixelList = new ArrayList<>();
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (count % sampleRate == 0) {
                    int rgb = image.getRGB(x, y);
                    int[] pixel = new int[3];
                    pixel[0] = (rgb >> 16) & 0xFF; // Red
                    pixel[1] = (rgb >> 8) & 0xFF; // Green
                    pixel[2] = rgb & 0xFF; // Blue
                    pixelList.add(pixel);
                }
                count++;
            }
        }
        return pixelList.toArray(new int[0][]);
    }

    private List<int[]> initializeCentroids(int[][] pixels, int numColors) {
        List<int[]> centroids = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numColors; i++) {
            centroids.add(pixels[random.nextInt(pixels.length)]);
        }
        return centroids;
    }

    private List<List<int[]>> assignToClusters(int[][] pixels, List<int[]> centroids) {
        List<List<int[]>> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        for (int[] pixel : pixels) {
            double minDistance = Double.MAX_VALUE;
            int closestCentroidIndex = 0;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = getDistance(pixel, centroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroidIndex = i;
                }
            }
            clusters.get(closestCentroidIndex).add(pixel);
        }
        return clusters;
    }

    private List<int[]> updateCentroids(List<List<int[]>> clusters) {
        List<int[]> newCentroids = new ArrayList<>();
        for (List<int[]> cluster : clusters) {
            if (cluster.isEmpty()) {
                // if a cluster is empty, re-initialize its centroid randomly
                newCentroids.add(
                        new int[] { new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256) });
                continue;
            }
            long[] sum = new long[3];
            for (int[] pixel : cluster) {
                sum[0] += pixel[0];
                sum[1] += pixel[1];
                sum[2] += pixel[2];
            }
            newCentroids.add(new int[] { (int) (sum[0] / cluster.size()), (int) (sum[1] / cluster.size()),
                    (int) (sum[2] / cluster.size()) });
        }
        return newCentroids;
    }

    private double getDistance(int[] p1, int[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2) + Math.pow(p1[2] - p2[2], 2));
    }
}