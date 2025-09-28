package com.ibm.txc.museum.shop.mcp;

import java.math.BigDecimal;

import com.ibm.txc.museum.shop.domain.Artwork;
import com.ibm.txc.museum.shop.domain.ShopItem;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class MuseumShopMcpServer {

    @Tool(name = "getInventoryItemsByCode", description = "List inventory items for an artwork by its title", structuredContent = true)
    public ToolResponse getInventoryItemsByCode(
            @ToolArg(description = "artwork codes like 'Girl with a Pearl Earring'") String name) {

        if (name == null || name.isBlank()) {
            return ToolResponse.error("Artwork name must be provided");
        }

        Artwork artwork = Artwork.find("name = ?1", name).firstResult();
        Log.info(artwork);
        ShopItem shopItem = ShopItem.find("id = ?1", artwork.id).firstResult();
        Log.info(shopItem);

        if (shopItem == null) {
            return ToolResponse.error("No shop item found for artwork name: " + name);
        }
        AvailableItems availableItems = new AvailableItems(
                shopItem.sku,
                shopItem.title,
                shopItem.artworkName,
                shopItem.price,
                shopItem.stock,
                shopItem.description);

        return ToolResponse.success(new TextContent(availableItems.toString()));
    }

    public record AvailableItems(String sku, String title, String artworkName, BigDecimal price, int stock,
            String description) {
    }

}
