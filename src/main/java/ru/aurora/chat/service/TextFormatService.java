package ru.aurora.chat.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextFormatService {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "(?is)<gradient:(?:&#|#)([0-9a-f]{6}):(?:&#|#)([0-9a-f]{6})>(.*?)</gradient>"
    );
    private static final Pattern MINIMESSAGE_HEX_PATTERN = Pattern.compile("(?i)<#([0-9a-f]{6})>");

    private final PlaceholderService placeholders;

    public TextFormatService(PlaceholderService placeholders) {
        this.placeholders = placeholders;
    }

    public String replacePlaceholders(Player player, String text) {
        return placeholders.apply(player, text);
    }

    public Component format(Player player, String text) {
        String resolved = normalizeHexTags(replacePlaceholders(player, text));
        Matcher matcher = GRADIENT_PATTERN.matcher(resolved);
        TextComponent.Builder result = Component.text();
        int currentIndex = 0;

        while (matcher.find()) {
            result.append(LEGACY_SERIALIZER.deserialize(resolved.substring(currentIndex, matcher.start())));
            result.append(createGradient("#" + matcher.group(1), "#" + matcher.group(2), matcher.group(3)));
            currentIndex = matcher.end();
        }

        result.append(LEGACY_SERIALIZER.deserialize(resolved.substring(currentIndex)));
        return result.build();
    }

    private String normalizeHexTags(String text) {
        return MINIMESSAGE_HEX_PATTERN.matcher(text).replaceAll("&#$1");
    }

    private Component createGradient(String startHex, String endHex, String text) {
        int[] codePoints = text.codePoints().toArray();
        if (codePoints.length == 0) {
            return Component.empty();
        }

        int start = Integer.parseInt(startHex.substring(1), 16);
        int end = Integer.parseInt(endHex.substring(1), 16);
        int startRed = (start >> 16) & 0xFF;
        int startGreen = (start >> 8) & 0xFF;
        int startBlue = start & 0xFF;
        int endRed = (end >> 16) & 0xFF;
        int endGreen = (end >> 8) & 0xFF;
        int endBlue = end & 0xFF;

        TextComponent.Builder gradient = Component.text();
        for (int index = 0; index < codePoints.length; index++) {
            double progress = codePoints.length == 1 ? 0.0D : (double) index / (codePoints.length - 1);
            TextColor color = TextColor.color(
                    interpolate(startRed, endRed, progress),
                    interpolate(startGreen, endGreen, progress),
                    interpolate(startBlue, endBlue, progress)
            );
            gradient.append(Component.text(new String(Character.toChars(codePoints[index]))).color(color));
        }

        return gradient.build();
    }

    private int interpolate(int start, int end, double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }
}
