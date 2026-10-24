package com.smartcare.hrms.common.basedao;

import com.smartcare.hrms.common.pagination.PaginationUtils;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseDashboardDAO {
    protected @Autowired EntityManager entityManager;

    protected @Autowired PaginationUtils paginationUtils;
}
