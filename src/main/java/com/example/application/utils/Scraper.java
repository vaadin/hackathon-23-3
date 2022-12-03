package com.example.application.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Scraper {
    public static WebsiteMetadata scrape(String url) {
        WebsiteMetadata metadata = new WebsiteMetadata();
        try {
            Document doc = Jsoup.connect(url).get();
            metadata.setTitle(doc.title());

            Element metaDescription = doc.select("meta[name='description']").first();
            if (metaDescription != null) {
                metadata.setDescription(metaDescription.attr("content"));
            }

            return metadata;
        } catch (Throwable t) {
            return metadata;
        }
    }
}
