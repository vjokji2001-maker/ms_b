package com.smartcare.hrms.hiring.dao;

import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.hiring.model.entity.PfChallan;
import com.smartcare.hrms.hiring.repository.PfChallanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Month;
import java.util.List;

@Repository
public class PfChallanDAO {

    private @Autowired
    EntityManager entityManager;

    private @Autowired
    PfChallanRepository pfChallanRepository;

    public List<EmpMonthlyPayrollSummary> getPayrollData(
            String financialYear,
            String month) {

        return getPayrollRecords(financialYear, month);
    }

    public List<EmpMonthlyPayrollSummary> getEmployeePayrollData(
            String financialYear,
            String month) {

        return getPayrollRecords(financialYear, month);
    }

    public List<EmpMonthlyPayrollSummary> getSummaryPayrollData(
            String financialYear,
            String month) {

        return getPayrollRecords(financialYear, month);
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

    public List<PfChallan> getGeneratedChallans(
            String financialYear,
            String month) {

        StringBuilder jpql = new StringBuilder();

        jpql.append("SELECT pc ");
        jpql.append("FROM PfChallan pc ");
        jpql.append("WHERE 1 = 1 ");

        String monthYear = null;

        if (financialYear != null
                && !financialYear.trim().isEmpty()) {

            jpql.append(
                    "AND pc.financialYear = :financialYear "
            );

            if (month != null
                    && !month.trim().isEmpty()) {

                monthYear =
                        getMonthYear(month, financialYear);

                jpql.append(
                        "AND pc.monthYear = :monthYear "
                );
            }
        }

        jpql.append(
                "ORDER BY pc.monthYear DESC, pc.id DESC"
        );

        TypedQuery<PfChallan> query =
                entityManager.createQuery(
                        jpql.toString(),
                        PfChallan.class
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

    public PfChallan getExistingChallan(
            String financialYear,
            String monthYear) {

        return pfChallanRepository
                .findByFinancialYearAndMonthYear(
                        financialYear,
                        monthYear
                )
                .orElse(null);
    }

    public PfChallan saveChallan(PfChallan challan) {
        return pfChallanRepository.save(challan);
    }

    private List<EmpMonthlyPayrollSummary> getPayrollRecords(
            String financialYear,
            String month) {

        StringBuilder jpql = new StringBuilder();

        jpql.append("SELECT eps ");
        jpql.append("FROM EmpMonthlyPayrollSummary eps ");
        jpql.append("JOIN FETCH eps.attendanceSalaryLink asl ");
        jpql.append(
                "WHERE UPPER(asl.payrollStatus) = 'PUBLISHED' "
        );

        String monthYear = null;

        if (financialYear != null
                && !financialYear.trim().isEmpty()) {

            String[] year =
                    getFinancialYear(financialYear);

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
                    getFinancialYear(financialYear);

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