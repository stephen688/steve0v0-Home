Component({
  properties: {
    title: { type: String, value: '内容暂时无法加载' },
    detail: { type: String, value: '请检查网络后重试' },
    action: { type: String, value: '重新加载' }
  },
  methods: {
    retry(this: MiniProgramComponentContext) {
      this.triggerEvent('retry');
    }
  }
});
