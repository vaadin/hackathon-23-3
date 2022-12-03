import { TextField, WebComponentModule as TextFieldWC } from '@hilla/react-components/TextField.js';
import { useEffect, useState } from 'react';
import { TextArea } from '@hilla/react-components/TextArea.js';
import { Button } from '@hilla/react-components/Button.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { BookmarkEndpoint } from 'Frontend/generated/endpoints';
import Bookmark from 'Frontend/generated/com/example/application/entities/Bookmark';
import { Notification } from '@hilla/react-components/Notification.js';
import { Link, useNavigate, useParams } from 'react-router-dom';
import WebsiteMetadata from 'Frontend/generated/com/example/application/utils/WebsiteMetadata';
import css from './EditView.module.css';

export default function EditView() {
  const [editedBookmark, setEditedBookmark] = useState<Bookmark>({ url: '', title: '', description: '' });
  const [websiteMetadata, setWebsiteMetadata] = useState<WebsiteMetadata | null>();
  const navigate = useNavigate();
  const params = useParams();
  const bookmarkIdParam = params.id;

  useEffect(() => {
    if (bookmarkIdParam) {
      BookmarkEndpoint.getBookmark(parseInt(bookmarkIdParam)).then((bookmark) => {
        if (bookmark) {
          setEditedBookmark(bookmark);
        }
      });
    }
  }, [bookmarkIdParam]);

  const handleSave = async () => {
    const bookmarkToSave = {
      ...editedBookmark,
      ...(websiteMetadata || {}),
    };
    await BookmarkEndpoint.save(bookmarkToSave);
    Notification.show('Bookmark saved', { theme: 'primary' });
    navigate('/');
  };

  const handleUrlChange = (e: Event) => {
    const url = (e.target as any).value;
    setEditedBookmark({
      ...editedBookmark,
      url,
    });
    if (url) {
      BookmarkEndpoint.scrape(url)
        .then(setWebsiteMetadata)
        .catch(() => setWebsiteMetadata({}));
    } else {
      setWebsiteMetadata({});
    }
  };

  return (
    <div className={css.form}>
      <TextField label="URL" value={editedBookmark.url} onInput={handleUrlChange}></TextField>
      <br />
      <TextField
        label="Title"
        helperText="Leave empty to use title from website"
        placeholder={websiteMetadata?.title}
        value={editedBookmark.title}
        onInput={(e) =>
          setEditedBookmark({
            ...editedBookmark,
            title: (e.target as TextFieldWC.TextField).value,
          })
        }
      ></TextField>
      <br />
      <TextArea
        label="Description"
        helperText="Leave empty to use description from website"
        placeholder={websiteMetadata?.description}
        value={editedBookmark.description}
        onInput={(e) =>
          setEditedBookmark({
            ...editedBookmark,
            description: (e.target as TextFieldWC.TextField).value,
          })
        }
      ></TextArea>
      <br />
      <HorizontalLayout theme="spacing">
        <Button theme="primary" onClick={handleSave}>
          Save
        </Button>
        <Link to="/">
          <Button>Cancel</Button>
        </Link>
      </HorizontalLayout>
    </div>
  );
}
