package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/book")
public class BookResource {

    @Inject
    TextAnalyticsService service;

    @GET
    @Path("/word-count")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getWordCount() {
        return Map.of("totalWords", service.getWordCount());
    }

    @GET
    @Path("/top-words")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getTopWords(@QueryParam("limit") int limit) {
        return service.getTopWords(limit > 0 ? limit : 10);
    }

    @GET
    @Path("/people")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getPeople() {
        return service.findPeople();
    }

    @GET
    @Path("/interaction-graph")
    @Produces(MediaType.APPLICATION_JSON)
    public TextAnalyticsService.GraphData getInteractionGraph() {
        return service.getInteractionGraph();
    }

    @GET
    @Path("/interaction-graph/view")
    @Produces("image/svg+xml")
    public Response getInteractionGraphSvg() {
        TextAnalyticsService.GraphData graphData = service.getInteractionGraph();
        String svg = generateSvg(graphData);
        return Response.ok(svg).build();
    }

    // Helper method to generate an SVG from graph data
    private String generateSvg(TextAnalyticsService.GraphData graphData) {
        StringBuilder svg = new StringBuilder();
        int width = 1000, height = 800;
        svg.append(String.format(
                "<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" style=\"background-color:#f0f0f0; font-family: sans-serif;\">",
                width, height));

        // Node positions in a circle
        Map<String, int[]> positions = new HashMap<>();
        int numNodes = graphData.nodes().size();
        int centerX = width / 2, centerY = height / 2, radius = 300;
        for (int i = 0; i < numNodes; i++) {
            String id = graphData.nodes().get(i).id();
            double angle = 2 * Math.PI * i / numNodes;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            positions.put(id, new int[] { x, y });
        }

        // Draw edges
        svg.append("<g id=\"edges\">");
        for (var edge : graphData.edges()) {
            int[] pos1 = positions.get(edge.source());
            int[] pos2 = positions.get(edge.target());
            String color = edge.sentiment() > 0.5 ? "#2a9d8f" : (edge.sentiment() < -0.5 ? "#e76f51" : "#8d99ae");
            double strokeWidth = 1 + Math.log(edge.weight()); // Log scale for better visibility
            svg.append(String.format(
                    "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"%.2f\" />",
                    pos1[0], pos1[1], pos2[0], pos2[1], color, strokeWidth));
        }
        svg.append("</g>");

        // Draw nodes
        svg.append("<g id=\"nodes\">");
        for (var node : graphData.nodes()) {
            int[] pos = positions.get(node.id());
            svg.append(String.format(
                    "<circle cx=\"%d\" cy=\"%d\" r=\"30\" fill=\"#edf2f4\" stroke=\"#2b2d42\" stroke-width=\"2\" />",
                    pos[0], pos[1]));
            svg.append(String.format(
                    "<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" dy=\".3em\" fill=\"#2b2d42\" font-size=\"14\">%s</text>",
                    pos[0], pos[1], node.label()));
        }
        svg.append("</g>");
        svg.append("</svg>");
        return svg.toString();
    }
}