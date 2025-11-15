// badge-generator.js

/**
 * Escapes XML/HTML special characters to prevent injection and parsing errors
 */
function escapeXml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&apos;');
}

/**
 * Estimates text width more accurately by considering character widths
 */
function estimateTextWidth(text) {
    if (!text) return 0;
    let width = 0;
    for (let i = 0; i < text.length; i++) {
        const char = text[i];
        // Approximate character widths (Verdana 12px)
        if (char === 'i' || char === 'l' || char === 'I' || char === '|' || char === ' ') {
            width += 3;
        } else if (char === 'm' || char === 'w' || char === 'M' || char === 'W') {
            width += 9;
        } else if (char >= 'A' && char <= 'Z') {
            width += 7;
        } else {
            width += 6; // default for lowercase and other chars
        }
    }
    return width;
}

export function createBadge(label, value, theme, iconBase64) {
    // Validate and sanitize inputs
    label = String(label || '').trim();
    value = String(value || '').trim();
    theme = String(theme || 'default').toLowerCase();
    
    const themes = {
        default: { bg: "#4695EB", text: "#ffffff" },
        native:  { bg: "#2EBAAE", text: "#ffffff" },
        dark:    { bg: "#0D1C2C", text: "#e0e0e0" },
        ai:      { bg: "#9b51e0", text: "#fff" }
    };
  
    const t = themes[theme] || themes.default;
  
    // Constants
    const iconSize = 16;
    const iconPadding = 4;
    const textPadding = 8;
    const textY = 16; // Vertical center for 12px font
    const fontSize = 12;
    const height = 24;
    
    // Calculate widths
    const labelTextWidth = estimateTextWidth(label);
    const valueTextWidth = estimateTextWidth(value);
    const labelWidth = labelTextWidth + (textPadding * 2);
    const valueWidth = valueTextWidth + (textPadding * 2);
    
    // Prepare embedded Quarkus icon
    const iconHref = `data:image/svg+xml;base64,${iconBase64}`;
    
    // Escape text content for XML safety
    const escapedLabel = escapeXml(label);
    const escapedValue = escapeXml(value);
    const escapedTitle = escapeXml(`${label}: ${value}`);
    
    // Calculate text positions
    const labelX = iconSize + iconPadding + textPadding;
    const valueX = labelX + labelWidth;
    
    // Calculate total width: from start to end of value text + its right padding
    const totalWidth = valueX + valueWidth;
  
    // Build SVG with proper XML structure
    return `<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="${totalWidth}" height="${height}" role="img">
  <title>${escapedTitle}</title>
  
  <rect width="${totalWidth}" height="${height}" rx="4" fill="${t.bg}"/>
  
  <image x="${iconPadding}" y="${iconPadding}" width="${iconSize}" height="${iconSize}" xlink:href="${iconHref}"/>
  
  <text x="${labelX}" y="${textY}" fill="${t.text}" font-family="Verdana,DejaVu Sans,sans-serif" font-size="${fontSize}">${escapedLabel}</text>
  <text x="${valueX}" y="${textY}" fill="${t.text}" font-family="Verdana,DejaVu Sans,sans-serif" font-size="${fontSize}">${escapedValue}</text>
</svg>`;
}
