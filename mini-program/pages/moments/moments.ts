import { api } from '../../services/api';
import { SITE_CONFIG } from '../../config/site';
import { MomentItem } from '../../types/api';
import { formatDateTime } from '../../utils/format';

interface PreparedMoment extends MomentItem {
  createdText: string;
  mediaTypeLabel: string;
  imageLayout: string;
}

type MomentOrder = 'latest' | 'earliest';

function prepareMoment(item: MomentItem): PreparedMoment {
  const images = item.images || [];
  return { ...item, images, createdText: formatDateTime(item.createdAt), mediaTypeLabel: '生活', imageLayout: images.length === 1 ? 'single' : images.length === 2 ? 'double' : 'grid' };
}

Page({
  data: {
    moments: [] as PreparedMoment[],
    page: 1,
    total: 0,
    hasMore: true,
    loading: true,
    loadingMore: false,
    error: false,
    errorMessage: '请检查网络后重试',
    heroImage: SITE_CONFIG.heroImage,
    sortOptions: ['最新', '最早'],
    sortIndex: 0,
    sortOrder: 'latest' as MomentOrder,
    sortLabel: '最新',
    sortArrow: '↓'
  },

  onLoad() { this.loadMoments(true); },
  onPullDownRefresh() { this.loadMoments(true); },
  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) this.loadMoments(false);
  },

  changeSort(event: WechatPageEvent) {
    const sortIndex = Number(event.detail?.value || 0);
    const sortOrder: MomentOrder = sortIndex === 1 ? 'earliest' : 'latest';
    if (sortOrder === this.data.sortOrder) return;
    this.setData({
      sortIndex,
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
    }).catch((error: Error) => this.setData({ loading: false, loadingMore: false, error: true, errorMessage: error.message || '请检查网络后重试' })).finally(() => wx.stopPullDownRefresh());
  },

  retry() { this.loadMoments(true); },

  previewImages(event: WechatPageEvent) {
    const images = event.detail?.images || [];
    const current = event.detail?.current || images[0];
    if (images.length) wx.previewImage({ current, urls: images });
  }
});
