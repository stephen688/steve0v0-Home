export function copyText(value: string, successMessage = '已复制到剪贴板'): void {
  if (!value) {
    wx.showToast({ title: '内容暂未填写', icon: 'none' });
    return;
  }
  wx.setClipboardData({
    data: value,
    success: () => wx.showToast({ title: successMessage, icon: 'none' })
  });
}
