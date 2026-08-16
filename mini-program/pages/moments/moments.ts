import { api } from '../../services/api';
import { SITE_CONFIG } from '../../config/site';
import { MomentItem } from '../../types/api';

interface PreparedMoment extends MomentItem {
  createdText: string;
  mediaTypeLabel: string;
  imageLayout: string;
}

type MomentOrder = 'latest' | 'earliest';

function formatMomentDateTime(value: string): string {
  const match = String(value || '').match(/^(\d{4})-(\d{2})-(\d{2})[T\s](\d{2}):(\d{2})/);
  if (!match) return '时间未知';
  return `${match[1]}年${Number(match[2])}月${Number(match[3])}日 ${match[4]}:${match[5]}`;
}

function resolveMomentImageUrl(value: string): string {
  const url = String(value || '').trim();
  if (!url || /^https?:\/\//i.test(url)) return url;
  const app = getApp<any>();
  const base = String(app?.globalData?.apiBase || '').replace(/\/$/, '');
  return `${base}${url.startsWith('/') ? url : `/${url}`}`;
}

function prepareMoment(item: MomentItem): PreparedMoment {
  const images = (item.images || []).map(resolveMomentImageUrl);
  const imageLayout = images.length === 1
    ? 'single'
    : images.length === 2
      ? 'double'
      : images.length === 4
        ? 'quad'
        : images.length === 9
          ? 'nine'
          : 'grid';
  return { ...item, images, createdText: formatMomentDateTime(item.createdAt), mediaTypeLabel: '生活', imageLayout };
}

Page({
  data: {
    moments: [] as PreparedMoment[],
    page: 1,
    total: 0,
    hasMore: true,
    loading: true,
    loadingMore: false,
    loadedOnce: false,
    error: false,
    errorMessage: '请检查网络后重试',
    heroImage: SITE_CONFIG.heroImage,
    sortOrder: 'latest' as MomentOrder,
    sortLabel: '最新',
    sortArrow: '↓'
  },

  onLoad() { this.loadMoments(true); },
  onShow() {
    if (this.data.loadedOnce) this.loadMoments(true);
  },
  onPullDownRefresh() { this.loadMoments(true); },
  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) this.loadMoments(false);
  },

  toggleSort() {
    if (this.data.loading || this.data.loadingMore) return;
    const sortOrder: MomentOrder = this.data.sortOrder === 'latest' ? 'earliest' : 'latest';
    this.setData({
      sortOrder,
      sortLabel: sortOrder === 'latest' ? '最新' : '最早',
      sortArrow: sortOrder === 'latest' ? '↓' : '↑',
      moments: [],
      page: 1,
      hasMore: true
    });
    this.loadMoments(true);
  },

  loadMoments(reset = false) {
    const page = reset ? 1 : this.data.page + 1;
    this.setData({ loading: reset, loadingMore: !reset, error: false });
    api.getMoments(page, 10, this.data.sortOrder).then((result) => {
      const existing = reset ? [] : this.data.moments;
      const ids = new Set(existing.map((item: PreparedMoment) => item.id));
      const incoming = result.list.map(prepareMoment).filter((item) => !ids.has(item.id));
      this.setData({ moments: [...existing, ...incoming], page: result.page, total: result.total, hasMore: result.hasMore, loading: false, loadingMore: false });
    }).catch((error: Error) => this.setData({ loading: false, loadingMore: false, error: true, errorMessage: error.message || '请检查网络后重试' })).finally(() => {
      this.setData({ loadedOnce: true });
      wx.stopPullDownRefresh();
    });
  },

  retry() { this.loadMoments(true); },

  previewImages(event: WechatPageEvent) {
    const images = event.detail?.images || [];
    const current = event.detail?.current || images[0];
    if (images.length) wx.previewImage({ current, urls: images });
  }
});
