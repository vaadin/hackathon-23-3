package com.example.application.endpoints;

import com.example.application.entities.Bookmark;
import com.example.application.repositories.BookmarkRepository;
import com.example.application.utils.Scraper;
import com.example.application.utils.WebsiteMetadata;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.List;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;

@Endpoint
@AnonymousAllowed
public class BookmarkEndpoint {

    private BookmarkRepository bookmarkRepository;

    public BookmarkEndpoint(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    public Bookmark getBookmark(int bookmarkId) {
        return bookmarkRepository.findById(bookmarkId).orElse(null);
    }

    public @Nonnull List<@Nonnull Bookmark> search(String query) {
        Bookmark exampleBookmark = new Bookmark();
        exampleBookmark.setUrl(query);
        exampleBookmark.setTitle(query);
        exampleBookmark.setDescription(query);
        ExampleMatcher matcher = ExampleMatcher.matchingAny()
                .withIgnoreCase("url", "title", "description")
                .withMatcher("url", contains())
                .withMatcher("title", contains())
                .withMatcher("description", contains());
        Example<Bookmark> example = Example.of(exampleBookmark, matcher);

        return bookmarkRepository.findAll(example);
    }

    public @Nonnull Bookmark save(@Nonnull Bookmark bookmark) {
        return bookmarkRepository.save(bookmark);
    }

    public void remove(int bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }

    public @Nonnull WebsiteMetadata scrape(String url) {
        return Scraper.scrape(url);
    }
}
