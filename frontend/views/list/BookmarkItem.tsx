import Bookmark from 'Frontend/generated/com/example/application/entities/Bookmark';
import css from './BookmarkItem.module.css';
import { Button } from '@hilla/react-components/Button.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { Link } from 'react-router-dom';

interface BookmarkItemProps {
  bookmark: Bookmark;
  onDelete: (bookmark: Bookmark) => void;
}

export function BookmarkItem({ bookmark, onDelete }: BookmarkItemProps) {
  const title = bookmark.title || bookmark.websiteTitle || bookmark.url;
  const description = bookmark.description || bookmark.websiteDescription;

  return (
    <div className={css.item}>
      <div className={css.content}>
        <a href={bookmark.url} target="_blank" rel="noopener">
          {title}
        </a>
        <br />
        {description && (
          <>
            <span className={css.description}>{bookmark.description}</span>
            <br />
          </>
        )}
      </div>
      <HorizontalLayout className={css.actions} theme="spacing">
        <Link to={`/edit/${bookmark.id}`}>
          <Button theme="secondary">Edit</Button>
        </Link>
        <Button theme="secondary error" onClick={() => onDelete(bookmark)}>
          Delete
        </Button>
      </HorizontalLayout>
    </div>
  );
}
