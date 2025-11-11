package com.example.gists;

import java.util.List;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final PolicyFactory policy;

    public MarkdownService() {
        var opts = new com.vladsch.flexmark.util.data.MutableDataSet();
        opts.set(Parser.EXTENSIONS, List.of(EmojiExtension.create()));
        parser = Parser.builder(opts).build();
        renderer = HtmlRenderer.builder(opts).escapeHtml(false).build();
        policy = new HtmlPolicyBuilder()
                .allowElements("a", "p", "pre", "code", "em", "strong", "ul", "ol", "li", "blockquote", "h1", "h2",
                        "h3", "hr", "br", "span")
                .allowAttributes("href").onElements("a")
                .allowUrlProtocols("http", "https")
                .toFactory();
    }

    public String toSafeHtml(String markdown) {
        var doc = parser.parse(markdown == null ? "" : markdown);
        return policy.sanitize(renderer.render(doc));
    }
}