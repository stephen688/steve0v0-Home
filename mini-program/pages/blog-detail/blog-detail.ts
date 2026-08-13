import { api } from '../../services/api';
import { ArticleDetail } from '../../types/api';
import { markdownToNodes, RichNode } from '../../utils/markdown';
import { copyText } from '../../utils/clipboard';
import { formatDate, parseTags } from '../../utils/format';

Page({
  data: {
    id: 0,
    article: null as ArticleDetail | null,
    tagList: [] as string[],
    publishedText: '',
    nodes: [] as RichNode[],
    outline: [] as { index: string; title: string }[],
    imageUrls: [] as string[],
    links: [] as { text: string; url: string }[],
    loading: true,
    error: false,
    errorMessage: '请稍后重试'
  },

  onLoad(options: { id?: string }) {
    const id = Number(options?.id || 0);
    this.setData({ id });
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
      this.setData({ article, tagList: parseTags(article.tags), publishedText: formatDate(article.publishedAt || article.createdAt, 'YYYY-MM-DD'), nodes: rendered.nodes, outline: rendered.outline, imageUrls: rendered.imageUrls, links: rendered.links, loading: false });
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

  copyLink(event: WechatPageEvent) {
    const index = Number(event.currentTarget?.dataset?.index || 0);
    const link = this.data.links[index];
    if (link) copyText(link.url, '链接已复制');
  },

  copyArticleUrl() {
    const article = this.data.article;
    if (article) copyText(`文章：${article.title}`, '文章标题已复制');
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
