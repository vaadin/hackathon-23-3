export type ViewInfo = Readonly<{
  title?: string;
  icon?: string;
}>;

export type ViewInfoMap = Record<string, ViewInfo | undefined>;

const views: ViewInfoMap = {
  '/hello': { icon: 'la la-globe', title: 'Hello React' },
  '/about': { icon: 'la la-file', title: 'About' },
};

export default views;
