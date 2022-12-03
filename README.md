# Hilla React Bookmarks

A minimal bookmark manager built with Hilla and React.

**Live Demo:** https://hilla-react-bookmarks.fly.dev/

**Screenshots**

![Screenshot](/screenshots/list.png?raw=true "Bookmark list")

![Screenshot](/screenshots/form.png?raw=true "Bookmark form")

### Summary

The app was generated from the current `hilla-react` preset.
It just contains two views, one for creating an editing bookmarks, and one for listing bookmarks.

The form does not use any form library, just plain event handlers and an edit state object.
Also means no validation at the moment.
A neat feature is that entering a URL loads the websites title and description, so that the user doesn't have to enter them manually.

The list view just shows all bookmarks that currently exist, no paging.
A search feature exists, that looks for the search query in URL, title and description.

The app uses an in-memory H2 database, and no authentication.
The live demo is deployed to fly.io.
