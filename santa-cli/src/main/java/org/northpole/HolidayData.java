package org.northpole;

import com.github.kusoroadeolu.clique.ansi.ColorCode;

/**
 * Helper class containing holiday-specific data including greetings, ASCII art, and color themes.
 */
public class HolidayData {

    public static record HolidayTheme(ColorCode borderColor, String quoteColor, String attributionColor) {
    }

    public static String[] getHolidayGreeting(String name, String holiday) {
        return switch (holiday) {
            case "christmas" -> new String[] {
                    "[red, bold]🎁 Merry Christmas, " + name + "![/]",
                    "[green]Peace on Earth and happy coding.[/]"
            };
            case "hanukkah" -> new String[] {
                    "[blue, bold]🕎 Happy Hanukkah, " + name + "![/]",
                    "[yellow]May your lights shine bright.[/]"
            };
            case "diwali" -> new String[] {
                    "[yellow, bold]🪔 Happy Diwali, " + name + "![/]",
                    "[magenta]May your terminal glow with joy.[/]"
            };
            case "yule" -> new String[] {
                    "[green, bold]🌲 Blessed Yule, " + name + "![/]",
                    "[cyan]Warmth to you in the long night.[/]"
            };
            case "eid" -> new String[] {
                    "[cyan, bold]🌙 Happy Eid, " + name + "![/]",
                    "[white]Wishing peace and renewal.[/]"
            };
            default -> new String[] {
                    "[magenta, bold]⭐️ Happy Holidays, " + name + "![/]",
                    "[cyan]Light and kindness to all.[/]"
            };
        };
    }

    public static HolidayTheme getHolidayTheme(String holiday) {
        return switch (holiday) {
            case "christmas" -> new HolidayTheme(ColorCode.RED, "green", "yellow");
            case "hanukkah" -> new HolidayTheme(ColorCode.BLUE, "blue", "*yellow");
            case "diwali" -> new HolidayTheme(ColorCode.YELLOW, "*yellow", "magenta");
            case "yule" -> new HolidayTheme(ColorCode.GREEN, "green", "cyan");
            case "eid" -> new HolidayTheme(ColorCode.CYAN, "cyan", "white");
            default -> new HolidayTheme(ColorCode.MAGENTA, "magenta", "cyan");
        };
    }

    public static String[] getHolidayArt(String holiday) {
        return switch (holiday) {
            case "christmas" -> getChristmasTree();
            case "hanukkah" -> getMenorah();
            case "diwali" -> getDiya();
            case "yule" -> getYuleTree();
            case "eid" -> getCrescentMoon();
            default -> getSparkles();
        };
    }

    private static String[] getChristmasTree() {
        return new String[] {
                "      [*yellow, bold]*[/]",
                "     [green, bold]/.\\[/]",
                "    [green, bold]/[/][*red]o[/][green, bold]..\\[/]",
                "    [green, bold]/..[/][*red]o[/][green, bold]\\[/]",
                "   [green, bold]/.[/][*red]o[/][green, bold]..[/][*red]o[/][green, bold]\\[/]",
                "   [green, bold]/...[/][*red]o[/][green, bold].\\[/]",
                "  [green, bold]/..[/][*red]o[/][green, bold]....\\[/]",
                "  [yellow]^^^[_]^^^[/]"
        };
    }

    private static String[] getMenorah() {
        return new String[] {
                "    [*yellow]║[/]",
                "   [*yellow]║║[/]",
                "  [*yellow]║║║[/]",
                " [*yellow]║║║║[/]",
                "[*yellow]║║║║║[/]",
                "   [blue]═══[/]"
        };
    }

    private static String[] getDiya() {
        return new String[] {
                "    [*yellow]╱╲[/]",
                "   [*yellow]╱  ╲[/]",
                "  [yellow]╱    ╲[/]",
                " [yellow]╱  [*yellow]◉[/]  ╲[/]",
                "[yellow]╱        ╲[/]",
                "    [*yellow]▓▓[/]"
        };
    }

    private static String[] getYuleTree() {
        return new String[] {
                "    [green]▲[/]",
                "   [green]▲▲▲[/]",
                "  [green]▲▲▲▲▲[/]",
                " [green]▲▲▲▲▲▲▲[/]",
                "[green]▲▲▲▲▲▲▲▲▲[/]",
                "   [yellow]█[/]"
        };
    }

    private static String[] getCrescentMoon() {
        return new String[] {
                "     [cyan]╱[/]",
                "    [cyan]╱[/]",
                "   [cyan]╱[/]",
                "  [cyan]╱[/]",
                " [cyan]╱[/]",
                "    [*white]★[/]"
        };
    }

    private static String[] getSparkles() {
        return new String[] {
                "    [*magenta]✦[/]",
                "   [*cyan]✦ [*yellow]✦[/]",
                "  [*green]✦ [*blue]✦ [*magenta]✦[/]",
                " [*yellow]✦ [*cyan]✦ [*green]✦ [*blue]✦[/]",
                "[*magenta]✦ [*yellow]✦ [*cyan]✦ [*green]✦ [*blue]✦[/]",
                "   [*magenta]*[/]"
        };
    }

    public static boolean hasDoubleWidthEmoji(String greetingLine) {
        // Check if first line has a double-width emoji (excluding 🕎 which is single-width)
        // Note: 🎄 and ⭐ have been replaced with 🎁 and ⭐️ to avoid width issues
        return greetingLine.contains("🪔") || greetingLine.contains("🌲") || greetingLine.contains("🌙");
    }
}

