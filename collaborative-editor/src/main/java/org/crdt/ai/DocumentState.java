package org.crdt.ai;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentState {

    private final List<CrdtCharacter> document = new CopyOnWriteArrayList<>();

    public void insert(int index, CrdtCharacter character) {
        if (index >= 0 && index <= document.size()) {
            document.add(index, character);
        }
    }

    public void delete(UUID characterId) {
        document.removeIf(c -> c.id().equals(characterId));
    }

    public String getTextContent() {
        return document.stream()
                .map(c -> String.valueOf(c.value()))
                .collect(Collectors.joining());
    }

    public List<CrdtCharacter> getFullDocument() {
        return List.copyOf(document);
    }
}
