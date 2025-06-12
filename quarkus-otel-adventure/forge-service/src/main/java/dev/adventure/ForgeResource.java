package dev.adventure;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/forge")
public class ForgeResource {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String craftSword() throws InterruptedException {
        // This custom span will appear nested inside the JAX-RS span
        workTheBellows();
        Thread.sleep(250); // Simulating hard work
        Span.current().addEvent("Sword is quenched.");
        return "Legendary Sword forged!";
    }

    @WithSpan("heating-the-metal") // Creates a new span for this method
    void workTheBellows() throws InterruptedException {
        Span.current().setAttribute("forge.temperature", "1315°C");
        Thread.sleep(150);
    }
}