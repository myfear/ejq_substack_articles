package org.northpole;

import com.github.kusoroadeolu.clique.Clique;
import com.github.kusoroadeolu.clique.config.BorderStyle;
import com.github.kusoroadeolu.clique.config.CellAlign;
import com.github.kusoroadeolu.clique.config.TableConfiguration;
import com.github.kusoroadeolu.clique.tables.Table;
import com.github.kusoroadeolu.clique.tables.TableType;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 🎄 A festive command-line application that spreads holiday cheer across cultures!
 * <p>
 * This command creates beautiful, colorful holiday cards in your terminal,
 * celebrating Christmas, Hanukkah, Diwali, Yule, Eid, and more. Each holiday
 * features unique ASCII art, themed colors, and inspirational developer quotes.
 * </p>
 * 
 * @author North Pole Development Team
 * @version 1.0
 */
@Command(name = "santa", mixinStandardHelpOptions = true, version = "santa-cli 1.0", description = "A festive CLI celebrating winter holidays around the world.")
public class SantaCommand implements Runnable {

    @Option(names = { "-n", "--name" }, defaultValue = "Friend")
    String name;

    @Option(names = { "-h", "--holiday" }, description = "Holiday type: christmas, hanukkah, diwali, yule, eid, lights")
    String holiday;

    @Option(names = { "-l", "--list" }, description = "Show a cheerful multi-faith gift list")
    boolean showList;

    /**
     * 🎁 Spreads holiday cheer by displaying personalized greetings and festive cards!
     * Orchestrates the display of gift lists and holiday quote cards based on user preferences.
     */
    @Override
    public void run() {
        String holidayType = holiday == null ? "lights" : holiday.toLowerCase();

        if (showList) {
            printGiftListTable();
        }

        printHolidayQuote(name, holidayType);
    }

    /**
     * 🎁 Displays a beautiful table of community gifts across different holidays.
     * Shows how diverse celebrations bring joy to everyone!
     */
    private void printGiftListTable() {
        Clique.parser().print("[bold, white]🎁 COMMUNITY GIFT LIST[/]\n");

        Clique.table(TableType.BOX_DRAW)
                .addHeaders("Person", "Holiday", "Gift")
                .addRows(
                        "Amit", "[yellow]Diwali[/]", "LED Fairy Lights",
                        "Sarah", "[blue]Hanukkah[/]", "Chocolate Gelt",
                        "Jonas", "[green]Christmas[/]", "Java 21 Book",
                        "Freya", "[cyan]Yule[/]", "Warm Wool Scarf",
                        "Layla", "[cyan]Eid[/]", "Sweet Treats")
                .render();

        System.out.println();
    }

    /**
     * ✨ Creates a stunning holiday card with ASCII art, personalized greetings, and inspirational quotes!
     * <p>
     * Each card features:
     * <ul>
     *   <li>🎨 Holiday-themed colors and borders</li>
     *   <li>🎭 Unique ASCII art for each celebration</li>
     *   <li>💬 Personalized greeting with the recipient's name</li>
     *   <li>📜 An inspirational developer quote</li>
     * </ul>
     * </p>
     * 
     * @param name the name of the person receiving the holiday greeting
     * @param holiday the type of holiday to celebrate (christmas, hanukkah, diwali, yule, eid, or lights)
     */
    private void printHolidayQuote(String name, String holiday) {
        // Create a holiday card design with ASCII art and festive colors
        Clique.parser().print("\n");

        // Get holiday-specific styling
        HolidayData.HolidayTheme theme = HolidayData.getHolidayTheme(holiday);

        BorderStyle style = BorderStyle.immutableBuilder()
                .horizontalBorderStyles(theme.borderColor())
                .verticalBorderStyles(theme.borderColor())
                .edgeBorderStyles(theme.borderColor())
                .build();

        TableConfiguration configuration = TableConfiguration
                .immutableBuilder()
                .borderStyle(style)
                .parser(Clique.parser())
                .alignment(CellAlign.LEFT)
                .padding(2)
                .build();

        Table card = Clique.table(TableType.BOX_DRAW, configuration);

        // Add header with first line of holiday greeting (required before adding rows)
        String[] greeting = HolidayData.getHolidayGreeting(name, holiday);
        card.addHeaders(greeting[0]);

        // Add second line of greeting as first row, with padding to compensate for
        // emoji width
        // Some emojis are double-width (2 columns) while others like 🕎 are
        // single-width (1 column)
        String secondLine = greeting[1];
        if (HolidayData.hasDoubleWidthEmoji(greeting[0])) {
            // Add padding inside the markup to compensate for emoji's 2-column width
            if (secondLine.startsWith("[")) {
                int endTag = secondLine.indexOf("]");
                if (endTag > 0) {
                    String colorTag = secondLine.substring(0, endTag + 1);
                    String content = secondLine.substring(endTag + 1);
                    // Add a space character as part of the styled content to pad for emoji width
                    secondLine = colorTag + " " + content;
                }
            } else {
                secondLine = " " + secondLine;
            }
        }
        card.addRows(secondLine);

        // Add spacing
        card.addRows("");

        // Add ASCII art based on holiday
        String[] art = HolidayData.getHolidayArt(holiday);
        for (String line : art) {
            card.addRows(line);
        }

        // Add spacing
        card.addRows("");

        // Add quote
        card.addRows("[" + theme.quoteColor() + ", bold]\"In every line of code,[/]");
        card.addRows("[" + theme.quoteColor() + ", bold]let there be a little more light.\"[/]");

        // Add spacing
        card.addRows("");

        // Add attribution
        card.addRows("[" + theme.attributionColor() + ", italic]— Seasonal Developer Wisdom[/]");

        card.render();
        Clique.parser().print("\n");
    }

}