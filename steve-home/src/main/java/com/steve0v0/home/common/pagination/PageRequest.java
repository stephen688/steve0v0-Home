package com.steve0v0.home.common.pagination;

import com.steve0v0.home.common.constant.Constants;
import lombok.Data;

@Data
public class PageRequest {
    private int page = 1;
    private int size = Constants.DEFAULT_PAGE_SIZE;

    public int getPage() {
        return Math.max(1, page);
    }

    public int getSize() {
        return Math.min(Math.max(1, size), Constants.MAX_PAGE_SIZE);
    }
}
