Component({
  properties: {
    rows: { type: Number, value: 3 }
  },
  data: {
    rowList: [0, 1, 2]
  },
  lifetimes: {
    attached(this: MiniProgramComponentContext) {
      const rows = Math.max(1, Number(this.data.rows) || 1);
      this.setData({ rowList: Array.from({ length: rows }, (_, index) => index) });
    }
  }
});
