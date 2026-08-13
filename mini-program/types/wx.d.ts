declare const wx: any;
declare function App(options: any): any;
declare function Page(options: any): any;
declare function Component(options: any): any;
declare function getApp<T = any>(): T;

declare interface MiniProgramComponentContext {
  data: any;
  setData(data: Record<string, any>): void;
  triggerEvent(name: string, detail?: any): void;
}

interface WechatPageEvent {
  currentTarget?: { dataset?: Record<string, any> };
  target?: { dataset?: Record<string, any> };
  detail?: any;
}
