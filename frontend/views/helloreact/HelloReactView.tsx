import { Button } from '@hilla/react-components/Button.js';
import { Notification } from '@hilla/react-components/Notification.js';
import { TextField } from '@hilla/react-components/TextField.js';
import * as cn from 'classnames';
import { HelloReactEndpoint } from 'Frontend/generated/endpoints';
import { useState } from 'react';
import css from './HelloReactView.module.css';
import img from './placeholder.avif';

export default function HelloReactView() {
  const [name, setName] = useState('');
  const [notification, setNotification] = useState('');

  return (
    <>
      <section className={cn('flex', 'p-m', 'gap-m', 'items-end')}>
        <TextField
          label="Your name"
          onValueChanged={(e) => {
            setName(e.detail.value);
          }}
        />
        <Button
          onClick={async () => {
            const serverResponse = await HelloReactEndpoint.sayHello(name);
            setNotification(serverResponse);
          }}
        >
          Say hello
        </Button>
        <Notification
          opened={!!notification}
          renderer={() => <>{notification}</>}
          onOpenedChanged={(e) => {
            if (!e.detail.value) {
              setNotification('');
            }
          }}
        />
      </section>
      <section className={css.placeholder}>
        <img src={img} alt="placeholder" />
      </section>
    </>
  );
}
