import { useReducer } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { defaultState, LocationContext, reducer } from './locationStore.js';
import views from './views.js';
import { Button } from '@hilla/react-components/Button.js';
import css from './MainLayout.module.css';

export default function MainLayout() {
  const { pathname } = useLocation();
  const [state, dispatch] = useReducer(reducer, {
    ...defaultState,
    path: pathname,
    title: views[pathname]?.title ?? '',
  });

  const context = {
    dispatch,
    state,
  };

  return (
    <LocationContext.Provider value={context}>
      <div className={css.container}>
        <header className={css.header}>
          <h1>Hilla Bookmarks</h1>
          <div>
            <Link to="/add">
              <Button theme="primary">Add bookmark</Button>
            </Link>
          </div>
        </header>

        <Outlet />
      </div>
    </LocationContext.Provider>
  );
}
