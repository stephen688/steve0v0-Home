Component({
  properties: {
    label: { type: String, value: '退出阅读' }
  },
  methods: {
    exit(this: MiniProgramComponentContext) {
      this.triggerEvent('exit');
    }
  }
});
