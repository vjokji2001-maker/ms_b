package com.smartcare.hrms.common.pagination;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationInfo {
    private int totalItems;
    private int currentPage;
    private int pageSize;
    private DataSorting dataSorting;
}
