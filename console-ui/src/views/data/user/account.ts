export interface Account {
  uid: string;
  platform: string;
  username: string;
  typeList: Array<{ type: string; uid: string}>;
}
