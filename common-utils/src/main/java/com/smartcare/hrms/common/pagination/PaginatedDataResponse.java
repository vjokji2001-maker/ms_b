package com.smartcare.hrms.common.pagination;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Data
public class PaginatedDataResponse <T>{

    private List<ColumnMeta> columns;
    private List<T> dataList;
    private PaginationInfo pagination;
}
