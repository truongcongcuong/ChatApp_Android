package com.example.chatapp.utils;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.nibor.autolink.Span;

import java.util.EnumSet;

public class MyAutoLink {

    private MyAutoLink() {
    }

    private static final LinkExtractor linkExtractor = LinkExtractor.builder()
            .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW, LinkType.EMAIL))
            .build();

    public static boolean isContainsLink(String input) {
        if (input == null)
            return false;
        if (input.trim().isEmpty())
            return false;
        Iterable<Span> spans = linkExtractor.extractSpans(input);

        for (Span span : spans) {
            if (span instanceof LinkSpan) {
                return true;
            }
        }

        return false;
    }
}
