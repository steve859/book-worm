package com.bookstore.service;

import com.bookstore.dto.response.MonthlyDebtReportDetailResponse;
import com.bookstore.entity.MonthlyDebtReportDetails;
import com.bookstore.entity.MonthlyDebtReports;
import com.bookstore.entity.Users;
import com.bookstore.mapper.MonthlyDebtReportDetailMapper;
import com.bookstore.repository.MonthlyDebtReportDetailRepository;
import com.bookstore.repository.MonthlyDebtReportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MonthlyDebtReportDetailService {

        MonthlyDebtReportDetailRepository monthlyDebtReportDetailRepository;
        MonthlyDebtReportDetailMapper monthlyDebtReportDetailMapper;
        MonthlyDebtReportRepository monthlyDebtReportRepository;
        MonthlyDebtReportService monthlyDebtReportService;

        public MonthlyDebtReportDetailService(MonthlyDebtReportDetailRepository monthlyDebtReportDetailRepository,
                        MonthlyDebtReportDetailMapper monthlyDebtReportDetailMapper,
                        MonthlyDebtReportRepository monthlyDebtReportRepository,
                        MonthlyDebtReportService monthlyDebtReportService) {
                this.monthlyDebtReportDetailRepository = monthlyDebtReportDetailRepository;
                this.monthlyDebtReportDetailMapper = monthlyDebtReportDetailMapper;
                this.monthlyDebtReportRepository = monthlyDebtReportRepository;
                this.monthlyDebtReportService = monthlyDebtReportService;
        }

        public MonthlyDebtReportDetailResponse createMonthlyDebtReportDetail(String userId, BigDecimal amount,
                        String type) {
                MonthlyDebtReportDetails monthlyDebtReportDetail = MonthlyDebtReportDetails.builder()
                                .userId(userId)
                                .amount(amount)
                                .type(type)
                                .reportDate(LocalDate.now())
                                .build();
                MonthlyDebtReports monthlyDebtReport = monthlyDebtReportRepository
                                .findByUserIdAndReportMonth(userId,
                                                monthlyDebtReportDetail.getReportDate()
                                                                .withDayOfMonth(monthlyDebtReportDetail.getReportDate()
                                                                                .lengthOfMonth()))
                                .orElse(null);
                if (monthlyDebtReport == null) {
                        monthlyDebtReportService.createMonthlyDebtReport(userId,
                                        monthlyDebtReportDetail.getReportDate().withDayOfMonth(
                                                        monthlyDebtReportDetail.getReportDate().lengthOfMonth()));
                        monthlyDebtReport = monthlyDebtReportRepository
                                        .findByUserIdAndReportMonth(userId, monthlyDebtReportDetail.getReportDate()
                                                        .withDayOfMonth(monthlyDebtReportDetail.getReportDate()
                                                                        .lengthOfMonth()))
                                        .orElse(null);
                }
                monthlyDebtReportDetail.setDebtReport(monthlyDebtReport);
                monthlyDebtReportDetailRepository.save(monthlyDebtReportDetail);
                monthlyDebtReportService.updateMonthlyDebtReport(monthlyDebtReportDetail, amount, type);
                return monthlyDebtReportDetailMapper.toMonthlyDebtReportDetailResponse(monthlyDebtReportDetail);
        }

        public MonthlyDebtReportDetailResponse reversePaymentDetail(String userId, BigDecimal amount) {
                log.info("Creating payment reversal detail for user {} amount {}", userId, amount);

                MonthlyDebtReportDetails monthlyDebtReportDetail = MonthlyDebtReportDetails.builder()
                                .userId(userId)
                                .amount(amount)
                                .type("Credit_Reversal") // Different type to identify reversals
                                .reportDate(LocalDate.now())
                                .build();
                MonthlyDebtReports monthlyDebtReport = monthlyDebtReportRepository
                                .findByUserIdAndReportMonth(userId,
                                                monthlyDebtReportDetail.getReportDate()
                                                                .withDayOfMonth(monthlyDebtReportDetail.getReportDate()
                                                                                .lengthOfMonth()))
                                .orElse(null);
                if (monthlyDebtReport == null) {
                        monthlyDebtReportService.createMonthlyDebtReport(userId,
                                        monthlyDebtReportDetail.getReportDate().withDayOfMonth(
                                                        monthlyDebtReportDetail.getReportDate().lengthOfMonth()));
                        monthlyDebtReport = monthlyDebtReportRepository
                                        .findByUserIdAndReportMonth(userId, monthlyDebtReportDetail.getReportDate()
                                                        .withDayOfMonth(monthlyDebtReportDetail.getReportDate()
                                                                        .lengthOfMonth()))
                                        .orElse(null);
                }
                monthlyDebtReportDetail.setDebtReport(monthlyDebtReport);
                monthlyDebtReportDetailRepository.save(monthlyDebtReportDetail);

                // Use the specific reversePayment method instead of generic
                // updateMonthlyDebtReport
                monthlyDebtReportService.reversePayment(monthlyDebtReportDetail, amount);
                return monthlyDebtReportDetailMapper.toMonthlyDebtReportDetailResponse(monthlyDebtReportDetail);
        }
}
