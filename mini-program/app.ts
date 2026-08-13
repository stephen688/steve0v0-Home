import { ENV } from './config/env';

App({
  globalData: {
    apiBase: ENV.apiBase,
    bootedAt: Date.now()
  },
  onLaunch() {
    wx.setStorageSync('steve-home-api-base', ENV.apiBase);
  }
});
