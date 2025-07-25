package com.bookstore.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.dto.response.MonthlyDebtReportResponse;
import com.bookstore.entity.MonthlyDebtReportDetails;
import com.bookstore.entity.MonthlyDebtReports;
import com.bookstore.entity.Users;
import com.bookstore.exception.AppException;
import com.bookstore.exception.ErrorCode;
import com.bookstore.mapper.MonthlyDebtReportMapper;
import com.bookstore.repository.MonthlyDebtReportRepository;
import com.bookstore.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MonthlyDebtReportService {
        @Autowired
        MonthlyDebtReportRepository monthlyDebtReportRepository;
        MonthlyDebtReportMapper monthlyDebtReportMapper;
        @Autowired
        private UserRepository userRepository;

        public MonthlyDebtReportResponse createMonthlyDebtReport(String userId, LocalDate reportDate) {
                Users user = userRepository.findById(userId).orElse(null);
                MonthlyDebtReports monthlyDebtReports = MonthlyDebtReports.builder()
                        .userId(userId)
                        .reportMonth(reportDate.withDayOfMonth(reportDate.lengthOfMonth()))
                        .openingDebt(user != null && user.getDebtAmount() != null ? user.getDebtAmount()
                                : BigDecimal.ZERO)
                        .debtIncrease(BigDecimal.ZERO)
                        .debtPayment(BigDecimal.ZERO)
                        .closingDebt(user != null && user.getDebtAmount() != null ? user.getDebtAmount()
                                : BigDecimal.ZERO)
                        .details(new ArrayList<>())
                        .build();
                return monthlyDebtReportMapper
                        .toMonthlyDebtReportResponse(monthlyDebtReportRepository.save(monthlyDebtReports));
        }

        public MonthlyDebtReportResponse updateMonthlyDebtReport(MonthlyDebtReportDetails monthlyDebtReportDetail,
                                                                 BigDecimal amount, String type) {
                MonthlyDebtReports monthlyDebtReport = monthlyDebtReportRepository
                        .findByUserIdAndReportMonth(monthlyDebtReportDetail.getUserId(),
                                monthlyDebtReportDetail.getReportDate()
                                        .withDayOfMonth(monthlyDebtReportDetail.getReportDate()
                                                .lengthOfMonth()))
                        .orElseThrow(() -> new AppException(ErrorCode.MONTHLY_DEBT_REPORT_NOT_EXISTED));
                if (type.equals("Debit")) {
                        monthlyDebtReport.setDebtIncrease(monthlyDebtReport.getDebtIncrease().add(amount));
                } else {
                        monthlyDebtReport.setDebtPayment(monthlyDebtReport.getDebtPayment().add(amount));
                }
                List<MonthlyDebtReportDetails> details = monthlyDebtReport.getDetails();
                details.add(monthlyDebtReportDetail);
                monthlyDebtReport.setDetails(details);
                monthlyDebtReport.setClosingDebt(
                        monthlyDebtReport.getOpeningDebt().add(monthlyDebtReport.getDebtIncrease())
                                .subtract(monthlyDebtReport.getDebtPayment()));
                return monthlyDebtReportMapper
                        .toMonthlyDebtReportResponse(monthlyDebtReportRepository.save(monthlyDebtReport));
        }

        public List<MonthlyDebtReportResponse> getMonthlyDebtReports(LocalDate reportMonth) {
                int month = reportMonth.getMonthValue();
                int year = reportMonth.getYear();
                List<MonthlyDebtReports> reports = monthlyDebtReportRepository.findAllByReportMonth(year, month);

                return reports.stream().map(r -> {
                        Users u = userRepository.findById(r.getUserId()).orElse(null);
                        return MonthlyDebtReportResponse.builder()
                                .debtReportId(r.getDebtReportId())
                                .userId(r.getUserId())
                                .userName(u != null ? (u.getFirstName() + " " + u.getLastName()) : null)
                                .openingDebt(r.getOpeningDebt())
                                .debtIncrease(r.getDebtIncrease())
                                .paidAmount(r.getDebtPayment())
                                .closingDebt(r.getClosingDebt())
                                .reportMonth(r.getReportMonth())
                                .build();
                }).toList();
        }

        public MonthlyDebtReportResponse getMonthlyDebtReport(Integer debtReportId) {
                return monthlyDebtReportMapper.toMonthlyDebtReportResponse(
                        monthlyDebtReportRepository.findById(debtReportId)
                                .orElseThrow(() -> new RuntimeException(
                                        "monthly debt report not found")));
        }

        public MonthlyDebtReportResponse reversePayment(MonthlyDebtReportDetails monthlyDebtReportDetail,
                        BigDecimal amount) {
                log.info("Reversing payment for user {} amount {}",
                                monthlyDebtReportDetail.getUserId(), amount);

                MonthlyDebtReports monthlyDebtReport = monthlyDebtReportRepository
                                .findByUserIdAndReportMonth(monthlyDebtReportDetail.getUserId(),
                                                monthlyDebtReportDetail.getReportDate()
                                                                .withDayOfMonth(monthlyDebtReportDetail.getReportDate()
                                                                                .lengthOfMonth()))
                                .orElseThrow(() -> new AppException(ErrorCode.MONTHLY_DEBT_REPORT_NOT_EXISTED));

                // Reduce debtPayment by the amount
                BigDecimal currentPayment = monthlyDebtReport.getDebtPayment();
                BigDecimal newPayment = currentPayment.subtract(amount);
                monthlyDebtReport.setDebtPayment(newPayment);

                log.info("Payment reversal: {} -> {}", currentPayment, newPayment);

                List<MonthlyDebtReportDetails> details = monthlyDebtReport.getDetails();
                details.add(monthlyDebtReportDetail);
                monthlyDebtReport.setDetails(details);
                monthlyDebtReport.setClosingDebt(
                                monthlyDebtReport.getOpeningDebt().add(monthlyDebtReport.getDebtIncrease())
                                                .subtract(monthlyDebtReport.getDebtPayment()));
                return monthlyDebtReportMapper
                                .toMonthlyDebtReportResponse(monthlyDebtReportRepository.save(monthlyDebtReport));
        }
}
