package com.bookstore.repository;

import com.bookstore.entity.MonthlyDebtReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyDebtReportRepository extends JpaRepository<MonthlyDebtReports, Integer> {
    Optional<MonthlyDebtReports> findByUserIdAndReportMonth(String userId, LocalDate date);

    List<MonthlyDebtReports> findAllByReportMonth(LocalDate date);

    // Retrieve all reports where reportMonth is within the given month (any day)
    @Query("select r from MonthlyDebtReports r where month(r.reportMonth) = :month and year(r.reportMonth) = :year")
    List<MonthlyDebtReports> findAllByReportMonth(@Param("year") int year, @Param("month") int month);
}
