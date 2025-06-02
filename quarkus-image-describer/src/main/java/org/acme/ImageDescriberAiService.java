package org.acme;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
@ApplicationScoped
public interface ImageDescriberAiService {

    @SystemMessage("You are an expert image analyst. Describe this image like a poetic elf would - use flowing, archaic English with references to starlight, nature, and beauty..")
    @UserMessage("Describe this image.") //
    String describeImage(Image image); // Use Langchain4j's Image class
}