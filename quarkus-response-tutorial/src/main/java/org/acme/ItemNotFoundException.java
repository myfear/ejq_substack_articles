package org.acme;

public class ItemNotFoundException extends RuntimeException {
    private final String itemId;

    public ItemNotFoundException(String itemId) {
        super("Item with ID '" + itemId + "' was not found.");
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }
}
