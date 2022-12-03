import MainLayout from 'Frontend/views/MainLayout.js';
import ListView from 'Frontend/views/list/ListView.js';
import EditView from 'Frontend/views/edit/EditView.js';
import { createBrowserRouter } from 'react-router-dom';

const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      { path: '/', element: <ListView /> },
      {
        path: '/add',
        element: <EditView />,
      },
      {
        path: '/edit/:id',
        element: <EditView />,
      },
    ],
  },
]);

export default router;
