package com.smartcare.hrms.hiring.dao;

import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.hiring.model.entity.EsicChallan;
import com.smartcare.hrms.hiring.repository.EsicChallanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Month;
import java.util.List;

@Repository
public class EsicChallanDAO {

    private @Autowired
    EntityManager entityManager;

    private @Autowired
    EsicChallanRepository esicChallanRepository;

    public List<EmpMonthlyPayrollSummary> getPayrollData(
            String financialYear,
            String month) {

        return getPayrollRecords(
                financialYear,
                month
        );
    }

    public List<EmpMonthlyPayrollSummary> getEmployeePayrollData(
            String financialYear,
            String month) {

        return getPayrollRecords(
                financialYear,
                month
        );
    }

    public List<EmpMonthlyPayrollSummary> getSummaryPayrollData(
            String financialYear,
            String month) {

        return getPayrollRecords(
                financialYear,
                month
        );
    }

    public List<EmpMonthlyPayrollSummary> getPayrollRecordsForGeneration(
            String financialYear,
            String month) {

        String monthYear = getMonthYear(
                month,
                financialYear
        );

        TypedQuery<EmpMonthlyPayrollSummary> query =
                entityManager.createQuery(
                        "SELECT eps " +
                                "FROM EmpMonthlyPayrollSummary eps " +
                                "JOIN FETCH eps.attendanceSalaryLink asl " +
                                "WHERE asl.monthYear = :monthYear " +
                                "ORDER BY asl.employeeId",
                        EmpMonthlyPayrollSummary.class
                );

        query.setParameter(
                "monthYear",
                monthYear
        );

        return query.getResultList();
    }

    public List<EsicChallan> getGeneratedChallans(
            String financialYear,
            String month) {

        StringBuilder jpql =
                new StringBuilder();

        jpql.append("SELECT ec ");
        jpql.append("FROM EsicChallan ec ");
        jpql.append("WHERE 1 = 1 ");

        String monthYear = null;

        if (financialYear != null
                && !financialYear.trim().isEmpty()) {

            jpql.append(
                    "AND ec.financialYear = :financialYear "
            );

            if (month != null
                    && !month.trim().isEmpty()) {

                monthYear =
                        getMonthYear(
                                month,
                                financialYear
                        );

                jpql.append(
                        "AND ec.monthYear = :monthYear "
                );
            }
        }

        jpql.append(
                "ORDER BY ec.monthYear DESC, ec.id DESC"
        );

        TypedQuery<EsicChallan> query =
                entityManager.createQuery(
                        jpql.toString(),
                        EsicChallan.class
                );

        if (financialYear != null
                && !financialYear.trim().isEmpty()) {

            query.setParameter(
                    "financialYear",
                    financialYear
            );
        }

        if (monthYear != null) {

            query.setParameter(
                    "monthYear",
                    monthYear
            );
        }

        return query.getResultList();
    }

    public EsicChallan getExistingChallan(
            String financialYear,
            String monthYear) {

        return esicChallanRepository
                .findByFinancialYearAndMonthYear(
                        financialYear,
                        monthYear
                )
                .orElse(null);
    }

    public EsicChallan saveChallan(
            EsicChallan challan) {

        return esicChallanRepository.save(
                challan
        );
    }

    private List<EmpMonthlyPayrollSummary>
    getPayrollRecords(
            String financialYear,
            String month) {

        StringBuilder jpql =
                new StringBuilder();

        jpql.append("SELECT eps ");
        jpql.append(
                "FROM EmpMonthlyPayrollSummary eps "
        );
        jpql.append(
                "JOIN FETCH eps.attendanceSalaryLink asl "
        );
        jpql.append(
                "WHERE UPPER(asl.payrollStatus) = 'PUBLISHED' "
        );

        String monthYear = null;

        if (financialYear != null
                && !financialYear.trim().isEmpty()) {

            String[] year =
                    getFinancialYear(
                            financialYear
                    );

            jpql.append(
                    "AND asl.monthYear BETWEEN :startMonth AND :endMonth "
            );

            if (month != null
                    && !month.trim().isEmpty()) {

                monthYear =
                        getMonthYear(
                                month,
                                financialYear
                        );

                jpql.append(
                        "AND asl.monthYear = :monthYear "
                );
            }
        }

        jpql.append(
                "ORDER BY asl.monthYear, asl.employeeId"
        );

        TypedQuery<EmpMonthlyPayrollSummary> query =
                entityManager.createQuery(
                        jpql.toString(),
                        EmpMonthlyPayrollSummary.class
                );

        if (financialYear != null
                && !financialYear.trim().isEmpty()) {

            String[] year =
                    getFinancialYear(
                            financialYear
                    );

            query.setParameter(
                    "startMonth",
                    year[0]
            );

            query.setParameter(
                    "endMonth",
                    year[1]
            );
        }

        if (monthYear != null) {

            query.setParameter(
                    "monthYear",
                    monthYear
            );
        }

        return query.getResultList();
    }

    private String[] getFinancialYear(
            String financialYear) {

        if (financialYear == null
                || !financialYear.matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Financial year must be in format YYYY-YY"
            );
        }

        int startYear =
                Integer.parseInt(
                        financialYear.substring(0, 4)
                );

        int endYear =
                startYear + 1;

        return new String[]{
                startYear + "-04",
                endYear + "-03"
        };
    }

    public String getResolvedMonthYear(
            String month,
            String financialYear) {

        return getMonthYear(
                month,
                financialYear
        );
    }

    private String getMonthYear(
            String month,
            String financialYear) {

        if (month == null
                || month.trim().isEmpty()) {

            throw new RuntimeException(
                    "Month is required"
            );
        }

        String value =
                month.trim();

        if (value.matches("\\d{4}-\\d{2}")) {

            return value;
        }

        if (financialYear == null
                || financialYear.trim().isEmpty()) {

            throw new RuntimeException(
                    "Financial year is required when month is provided"
            );
        }

        String[] parts =
                financialYear.split("-");

        int startYear =
                Integer.parseInt(parts[0]);

        int endYear =
                startYear + 1;

        Month selectedMonth;

        try {

            selectedMonth =
                    Month.valueOf(
                            value.toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            throw new RuntimeException(
                    "Invalid month: " + month
            );
        }

        int monthNumber =
                selectedMonth.getValue();

        int year =
                monthNumber >= 4
                        ? startYear
                        : endYear;

        return String.format(
                "%04d-%02d",
                year,
                monthNumber
        );
    }
}