import { createContext, Dispatch } from 'react';

const BASE = 'LOCATION';

export const SET = `${BASE}_SET` as const;

export type SetLocationAction = Readonly<{
  path: string;
  title: string;
  type: typeof SET;
}>;

export type LocationState = Readonly<{
  appName: string;
  path: string;
  title: string;
}>;

export function go(path: string, title: string, dispatch: Dispatch<SetLocationAction>): void {
  dispatch({
    path,
    title,
    type: SET,
  });
}

export function reducer(state: LocationState, action: SetLocationAction) {
  if (action.type === SET) {
    return {
      appName: state.appName,
      path: action.path,
      title: action.title,
    };
  }

  return state;
}

export const defaultState: LocationState = {
  appName: 'react-ref-hilla-app',
  path: '',
  title: '',
};

export type LocationData = Readonly<{
  state: LocationState;
  dispatch: Dispatch<SetLocationAction>;
}>;

export const LocationContext = createContext<LocationData>({
  dispatch: () => {},
  state: defaultState,
});
