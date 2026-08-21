//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import java.util.Locale;
import net.kyori.adventure.text.format.TextColor;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.TextComponent;
import java.util.regex.Matcher;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import java.util.regex.Pattern;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextFormatService
{
    private static final LegacyComponentSerializer LEGACY_SERIALIZER;
    private static final String GRADIENT_CLOSING_TAG = "</gradient>";
    private static final String HEX_COLOR = "(?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4})";
    private static final String GRADIENT_COLOR = "(?:&#|#)(?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4})";
    private static final Pattern GRADIENT_PATTERN;
    private static final Pattern GRADIENT_TAG_PATTERN;
    private static final Pattern GRADIENT_COLOR_PATTERN;
    private static final Pattern MINIMESSAGE_HEX_PATTERN;
    private final PlaceholderService placeholders;

    public TextFormatService(final PlaceholderService placeholders) {
        this.placeholders = placeholders;
    }

    public String replacePlaceholders(final Player player, final String text) {
        return this.placeholders.apply(player, text);
    }

    public Component format(final Player player, final String text) {
        String resolved = this.replacePlaceholders(player, text);
        resolved = this.normalizeHexTags(resolved);
        resolved = this.closeUnfinishedGradients(resolved);
        final Matcher matcher = TextFormatService.GRADIENT_PATTERN.matcher(resolved);
        final TextComponent.Builder result = Component.text();
        int currentIndex = 0;
        while (matcher.find()) {
            result.append((Component)TextFormatService.LEGACY_SERIALIZER.deserialize(resolved.substring(currentIndex, matcher.start())));
            result.append(this.createGradient(this.parseGradientColors(matcher.group(1)), matcher.group(2)));
            currentIndex = matcher.end();
        }
        result.append((Component)TextFormatService.LEGACY_SERIALIZER.deserialize(resolved.substring(currentIndex)));
        return (Component)result.build();
    }

    public String formatLegacy(final Player player, final String text) {
        return TextFormatService.LEGACY_SERIALIZER.serialize(this.format(player, text));
    }

    private String normalizeHexTags(final String text) {
        return TextFormatService.MINIMESSAGE_HEX_PATTERN.matcher(text).replaceAll("&#$1");
    }

    private String closeUnfinishedGradients(final String text) {
        final Matcher matcher = TextFormatService.GRADIENT_TAG_PATTERN.matcher(text);
        final StringBuilder result = new StringBuilder(text.length() + "</gradient>".length());
        int currentIndex = 0;
        boolean gradientOpen = false;
        while (matcher.find()) {
            result.append(text, currentIndex, matcher.start());
            final String tag = matcher.group();
            if ("</gradient>".equalsIgnoreCase(tag)) {
                result.append(tag);
                gradientOpen = false;
            }
            else {
                if (gradientOpen) {
                    result.append("</gradient>");
                }
                result.append(tag);
                gradientOpen = true;
            }
            currentIndex = matcher.end();
        }
        result.append(text, currentIndex, text.length());
        if (gradientOpen) {
            result.append("</gradient>");
        }
        return result.toString();
    }

    private List<Integer> parseGradientColors(final String value) {
        final Matcher matcher = TextFormatService.GRADIENT_COLOR_PATTERN.matcher(value);
        final List<Integer> colors = new ArrayList<Integer>();
        while (matcher.find()) {
            colors.add(this.parseColor(matcher.group(1)));
        }
        return colors;
    }

    private Component createGradient(final List<Integer> colors, final String text) {
        final int[] codePoints = text.codePoints().toArray();
        if (codePoints.length == 0 || colors.size() < 2) {
            return (Component)Component.empty();
        }
        final TextComponent.Builder gradient = Component.text();
        final int segments = colors.size() - 1;
        for (int index = 0; index < codePoints.length; ++index) {
            final double progress = (codePoints.length == 1) ? 0.0 : (index / (double)(codePoints.length - 1));
            final double position = progress * segments;
            final int segment = Math.min((int)position, segments - 1);
            final double segmentProgress = position - segment;
            final int color = this.interpolateColor(colors.get(segment), colors.get(segment + 1), segmentProgress);
            gradient.append(Component.text(new String(Character.toChars(codePoints[index]))).color(TextColor.color(color)));
        }
        return (Component)gradient.build();
    }

    private int interpolateColor(final int start, final int end, final double progress) {
        final int startRed = start >> 16 & 0xFF;
        final int startGreen = start >> 8 & 0xFF;
        final int startBlue = start & 0xFF;
        final int endRed = end >> 16 & 0xFF;
        final int endGreen = end >> 8 & 0xFF;
        final int endBlue = end & 0xFF;
        final int red = this.interpolate(startRed, endRed, progress);
        final int green = this.interpolate(startGreen, endGreen, progress);
        final int blue = this.interpolate(startBlue, endBlue, progress);
        return red << 16 | green << 8 | blue;
    }

    private int parseColor(final String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.length() == 3 || normalized.length() == 4) {
            normalized = "" + normalized.charAt(0) + normalized.charAt(0) + normalized.charAt(1) + normalized.charAt(1) + normalized.charAt(2) + normalized.charAt(2);
        }
        else if (normalized.length() == 8) {
            normalized = normalized.substring(0, 6);
        }
        return Integer.parseInt(normalized, 16);
    }

    private int interpolate(final int start, final int end, final double progress) {
        return (int)Math.round(start + (end - start) * progress);
    }

    static {
        LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('&').hexColors().useUnusualXRepeatedCharacterHexFormat().build();
        GRADIENT_PATTERN = Pattern.compile("(?is)<gradient:((?:&#|#)(?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4})(?::(?:&#|#)(?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4}))+)>((?s:.*?))</gradient>");
        GRADIENT_TAG_PATTERN = Pattern.compile("(?is)<gradient:(?:&#|#)(?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4})(?::(?:&#|#)(?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4}))+>|</gradient>");
        GRADIENT_COLOR_PATTERN = Pattern.compile("(?i)(?:&#|#)((?:[0-9a-f]{6}(?:[0-9a-f]{2})?|[0-9a-f]{3,4}))");
        MINIMESSAGE_HEX_PATTERN = Pattern.compile("(?i)<#([0-9a-f]{6})>");
    }
}
