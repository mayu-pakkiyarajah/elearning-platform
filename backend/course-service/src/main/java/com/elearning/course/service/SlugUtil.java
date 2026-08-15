package com.elearning.course.service;

import java.text.Normalizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private SlugUtil() {}

    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String noWhitespace = WHITESPACE.matcher(normalized).replaceAll("-");
        String slug = NON_LATIN.matcher(noWhitespace).replaceAll("");
        return slug.toLowerCase().replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }

    /**
     * Appends -2, -3, ... until `exists` returns false, so titles that collide
     * ("Intro to Java" twice) still get distinct, readable slugs.
     */
    public static String uniqueSlugify(String input, Predicate<String> exists) {
        String base = slugify(input);
        String candidate = base;
        int suffix = 2;
        while (exists.test(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
