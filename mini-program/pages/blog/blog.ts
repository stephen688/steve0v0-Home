import { api } from '../../services/api';
import { ArticleListItem } from '../../types/api';
import { formatDate, parseTags } from '../../utils/format';

interface PreparedArticle extends ArticleListItem {
  tagList: string[];
  publishedText: string;
}

function prepareArticle(item: ArticleListItem): PreparedArticle {
  return { ...item, tagList: parseTags(item.tags).slice(0, 5), publishedText: formatDate(item.publishedAt, 'YYYY-MM-DD') };
}

Page({
  data: {
    category: 'tech',
    total: 0,
    articles: [] as PreparedArticle[],
    page: 1,
    hasMore: true,
    loadedOnce: false,
    loading: true,
    loadingMore: false,
    error: false,
    errorMessage: '请检查网络后重试'
  },

  onLoad() {
    this.loadArticles(true);
  },

  onShow() {
    if (this.data.loadedOnce) this.loadArticles(true);
  },

  onPullDownRefresh() {
    this.loadArticles(true);
  },

  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) this.loadArticles(false);
  },

  switchCategory(event: WechatPageEvent) {
    const category = String(event.currentTarget?.dataset?.category || 'tech');
    if (category === this.data.category) return;
    this.setData({ category, articles: [], page: 1, hasMore: true });
    this.loadArticles(true);
  },

  loadArticles(reset = false) {
    const page = reset ? 1 : this.data.page + 1;
    this.setData({ loading: reset, loadingMore: !reset, error: false });
    api.getArticles(this.data.category, page, 10).then((result) => {
      const incoming = result.list.map(prepareArticle);
      const existing = reset ? [] : this.data.articles;
      const ids = new Set(existing.map((item: PreparedArticle) => item.id));
      const merged = [...existing, ...incoming.filter((item) => !ids.has(item.id))];
      this.setData({ articles: merged, total: result.total, page: result.page, hasMore: result.hasMore, loading: false, loadingMore: false });
    }).catch((error: Error) => {
      this.setData({ loading: false, loadingMore: false, error: true, errorMessage: error.message || '请检查网络后重试' });
    }).finally(() => {
      this.setData({ loadedOnce: true });
      wx.stopPullDownRefresh();
    });
  },

  retry() { this.loadArticles(true); },

  openArticle(event: WechatPageEvent) {
    const id = event.detail?.id || event.currentTarget?.dataset?.id;
    if (id) wx.navigateTo({ url: `/pages/blog-detail/blog-detail?id=${id}` });
  }
});
