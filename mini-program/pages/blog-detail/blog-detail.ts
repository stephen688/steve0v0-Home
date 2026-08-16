import { api } from '../../services/api';
import { ArticleDetail } from '../../types/api';
import { markdownToNodes, MarkdownBlock, RichNode } from '../../utils/markdown';
import { formatDateTime, parseTags } from '../../utils/format';

function resolveAssetUrl(value: string): string {
  const url = String(value || '').trim();
  if (!url || /^https?:\/\//i.test(url)) return url;
  const app = getApp<any>();
  const base = String(app?.globalData?.apiBase || '').replace(/\/$/, '');
  return `${base}${url.startsWith('/') ? url : `/${url}`}`;
}

function getNavigationMetrics() {
  try {
    const menuButton = wx.getMenuButtonBoundingClientRect();
    return {
      navHeight: Math.ceil(menuButton.bottom + 8),
      menuButtonTop: menuButton.top,
      menuButtonHeight: menuButton.height,
      menuButtonWidth: menuButton.width
    };
  } catch (_) {
    const system = wx.getSystemInfoSync();
    const statusBarHeight = system.statusBarHeight || 24;
    return {
      navHeight: statusBarHeight + 44,
      menuButtonTop: statusBarHeight + 4,
      menuButtonHeight: 32,
      menuButtonWidth: 87
    };
  }
}

Page({
  data: {
    id: 0,
    article: null as ArticleDetail | null,
    tagList: [] as string[],
    publishedText: '',
    coverImageUrl: '',
    nodes: [] as RichNode[],
    blocks: [] as MarkdownBlock[],
    imageUrls: [] as string[],
    navHeight: 76,
    menuButtonTop: 28,
    menuButtonHeight: 32,
    menuButtonWidth: 87,
    loading: true,
    error: false,
    errorMessage: '请稍后重试'
  },

  onLoad(options: { id?: string }) {
    const id = Number(options?.id || 0);
    this.setData({ id, ...getNavigationMetrics() });
    if (!id) {
      this.setData({ loading: false, error: true, errorMessage: '文章编号无效' });
      return;
    }
    this.loadArticle();
  },

  loadArticle() {
    this.setData({ loading: true, error: false });
    api.getArticle(this.data.id).then((article) => {
      const rendered = markdownToNodes(article.content || '');
      this.setData({
        article,
        tagList: parseTags(article.tags),
        publishedText: formatDateTime(article.publishedAt || article.createdAt),
        coverImageUrl: resolveAssetUrl(article.coverImage),
        nodes: rendered.nodes,
        blocks: rendered.blocks,
        imageUrls: rendered.imageUrls,
        loading: false
      });
    }).catch((error: Error) => {
      this.setData({ loading: false, error: true, errorMessage: error.message || '文章暂时无法加载' });
    });
  },

  retry() { this.loadArticle(); },

  previewImage(event: WechatPageEvent) {
    const current = event.currentTarget?.dataset?.src || this.data.imageUrls[0];
    if (!this.data.imageUrls.length) return;
    wx.previewImage({ current, urls: this.data.imageUrls });
  },

  handleRichTap(event: WechatPageEvent) {
    const src = event.detail?.src || event.detail?.currentTarget?.dataset?.src;
    if (src && this.data.imageUrls.includes(src)) wx.previewImage({ current: src, urls: this.data.imageUrls });
  },

  exitArticle() {
    wx.navigateBack({
      delta: 1,
      fail: () => {
        wx.switchTab({
          url: '/pages/blog/blog',
          fail: () => wx.reLaunch({ url: '/pages/blog/blog' })
        });
      }
    });
  }
});
