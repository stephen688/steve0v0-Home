const GLOW_PALETTE_COUNT = 8;

function articleGlowClass(item: { id?: number | string; title?: string } | null | undefined) {
  const articleId = Number(item?.id);
  if (Number.isFinite(articleId)) {
    return `article-card--glow-${Math.abs(Math.trunc(articleId)) % GLOW_PALETTE_COUNT}`;
  }

  const title = item?.title || '';
  let seed = 0;
  for (let index = 0; index < title.length; index += 1) {
    seed = (seed * 31 + title.charCodeAt(index)) >>> 0;
  }
  return `article-card--glow-${seed % GLOW_PALETTE_COUNT}`;
}

Component({
  data: {
    glowClass: 'article-card--glow-0'
  },
  properties: {
    item: {
      type: Object,
      value: {},
      observer(this: MiniProgramComponentContext, value: { id?: number | string; title?: string }) {
        this.setData({ glowClass: articleGlowClass(value) });
      }
    }
  },
  methods: {
    select(this: MiniProgramComponentContext) {
      this.triggerEvent('select', { id: this.data.item.id });
    }
  }
});
