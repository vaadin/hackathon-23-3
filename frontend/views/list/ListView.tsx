import { useEffect, useState } from 'react';
import { BookmarkEndpoint } from 'Frontend/generated/endpoints';
import Bookmark from 'Frontend/generated/com/example/application/entities/Bookmark';
import { BookmarkItem } from 'Frontend/views/list/BookmarkItem';
import { TextField } from '@hilla/react-components/TextField.js';
import { Notification } from '@hilla/react-components/Notification.js';
import { ConfirmDialog } from 'Frontend/views/list/ConfirmDialog';

export default function ListView() {
  const [query, setQuery] = useState('');
  const [bookmarks, setBookmarks] = useState<Array<Bookmark>>([]);
  const [bookmarkToDelete, setBookmarkToDelete] = useState<Bookmark | null>();

  useEffect(() => {
    BookmarkEndpoint.search(query).then(setBookmarks);
  }, [query]);

  const handleDeleteRequest = async (bookmark: Bookmark) => {
    setBookmarkToDelete(bookmark);
  };

  const handleDeleteConfirm = async () => {
    if (!bookmarkToDelete) return;
    await BookmarkEndpoint.remove(bookmarkToDelete.id!);
    BookmarkEndpoint.search(query).then(setBookmarks);
    Notification.show('Bookmark removed', { theme: 'primary' });
    setBookmarkToDelete(null);
  };

  const handleDeleteCancel = () => {
    setBookmarkToDelete(null);
  };

  return (
    <div>
      <TextField
        placeholder="Search"
        value={query}
        onInput={(e) => setQuery((e.target as HTMLInputElement).value)}
      ></TextField>
      <br />
      <br />
      <div>
        {bookmarks.map((bookmark) => (
          <BookmarkItem key={bookmark.id} bookmark={bookmark} onDelete={handleDeleteRequest} />
        ))}
      </div>
      <ConfirmDialog
        opened={!!bookmarkToDelete}
        title="Delete bookmark"
        content="Do you really want to delete the bookmark?"
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
      ></ConfirmDialog>
    </div>
  );
}
