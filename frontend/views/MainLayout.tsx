import { AppLayout } from '@hilla/react-components/AppLayout.js';
import { DrawerToggle } from '@hilla/react-components/DrawerToggle.js';
import { Scroller } from '@hilla/react-components/Scroller.js';
import * as cn from 'classnames';
import { PropsWithChildren, useReducer } from 'react';
import { Outlet, useHref, useLocation, useNavigate } from 'react-router-dom';
import { Nav, NavItem } from '../thirdParty.js';
import GameView from './game/GameView.js';
import { defaultState, LocationContext, reducer } from './locationStore.js';
import views from './views.js';


export default function MenuOnLeftLayout() {
  return (
    <GameView />
  );
}
