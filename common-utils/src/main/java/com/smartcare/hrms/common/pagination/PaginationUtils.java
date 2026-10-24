package com.smartcare.hrms.common.pagination;

import com.smartcare.hrms.common.enumeration.SortingOrder;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaginationUtils {

    public String getLimitSqlString(PaginationInfo paginationInfo){
        int currentPageNumber = paginationInfo.getCurrentPage();
        int pageSize = paginationInfo.getPageSize();
        int fromLimit = (currentPageNumber - 1) * pageSize;
        return " LIMIT "+pageSize+" OFFSET "+fromLimit;
    }

    public PaginatedDataResponse<?> getPaginatedResponse(List<ColumnMeta> columnMetaList,List<Object> dataList, PaginationInfo paginationInfo){
        return PaginatedDataResponse.builder()
                .columns(columnMetaList)
                .pagination(paginationInfo)
                .dataList(dataList)
                .build();
    }

    public PaginationInfo getPaginationInfoObject(){
        return PaginationInfo.builder()
                .currentPage(1)
                .pageSize(10)
                .build();
    }

    public String getSortingQuery(List<ColumnMeta> columnList,PaginationInfo paginationInfo){
        String sqlQuery =" ";
        DataSorting dataSorting = paginationInfo.getDataSorting();
        if(!ValidateUtils.isNullObject(dataSorting) && !ValidateUtils.isNullObject(dataSorting.getByColumn())){
            for (ColumnMeta columnMeta : columnList) {
                if(columnMeta.getField().equals(dataSorting.getByColumn().getField())){
                    if (SortingOrder.ASC.equals(dataSorting.getSortingOrder())) {
                        sqlQuery = " ORDER BY "+dataSorting.getByColumn().getField()+" "+SortingOrder.ASC.toString();
                    }else if(SortingOrder.DESC.equals(dataSorting.getSortingOrder())){
                        sqlQuery = " ORDER BY "+dataSorting.getByColumn().getField()+" "+SortingOrder.DESC.toString();
                    }
                }
            }
        }
        return sqlQuery;
    }
}
