package com.smartcare.hrms.common.pagination;

import com.smartcare.hrms.common.enumeration.SortingOrder;
import lombok.Getter;

@Getter
public class DataSorting {
    private SortingOrder sortingOrder;
    private ColumnMeta byColumn;
}
