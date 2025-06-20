package com.bookstore.repository;

import com.bookstore.entity.MonthlyInventoryReportDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyInventoryReportDetailRepository extends JpaRepository<MonthlyInventoryReportDetails, Integer> {
}
