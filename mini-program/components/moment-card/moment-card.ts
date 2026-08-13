Component({
  properties: {
    item: { type: Object, value: {} }
  },
  methods: {
    preview(this: MiniProgramComponentContext, event: WechatPageEvent) {
      this.triggerEvent('preview', {
        images: this.data.item.images || [],
        current: event.currentTarget?.dataset?.current
      });
    }
  }
});
