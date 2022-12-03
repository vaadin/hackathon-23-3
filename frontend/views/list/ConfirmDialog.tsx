import { Dialog } from '@hilla/react-components/Dialog.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { Button } from '@hilla/react-components/Button.js';

interface ConfirmDialogProps {
  title: string;
  content: string;
  opened: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog(props: ConfirmDialogProps) {
  const header = <h3 style={{ margin: 0 }}>{props.title}</h3>;
  const footer = (
    <HorizontalLayout theme="spacing">
      <Button theme="secondary" onClick={props.onCancel}>
        Cancel
      </Button>
      <Button theme="primary error" onClick={props.onConfirm}>
        Delete
      </Button>
    </HorizontalLayout>
  );

  return (
    <Dialog opened={props.opened} header={header} footer={footer} noCloseOnOutsideClick={true} noCloseOnEsc={true}>
      {props.content}
    </Dialog>
  );
}
