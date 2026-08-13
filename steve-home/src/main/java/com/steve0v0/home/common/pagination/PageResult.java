package com.steve0v0.home.common.pagination;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private int page;
    private int size;
    private long total;
    private boolean hasMore;
    private List<T> list;

    public PageResult(int page, int size, long total, List<T> list) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.hasMore = (long) page * size < total;
        this.list = list;
    }
}
