package com.bookstore.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.dto.request.PaymentReceiptCreationRequest;
import com.bookstore.dto.request.PaymentReceiptUpdateRequest;
import com.bookstore.dto.response.PaymentReceiptResponse;
import com.bookstore.entity.PaymentReceipts;
import com.bookstore.entity.Users;
import com.bookstore.exception.AppException;
import com.bookstore.exception.ErrorCode;
import com.bookstore.mapper.PaymentReceiptMapper;
import com.bookstore.repository.MonthlyDebtReportRepository;
import com.bookstore.repository.PaymentReceiptRepository;
import com.bookstore.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentReceiptService {
    @Autowired
    PaymentReceiptRepository paymentReceiptRepository;
    PaymentReceiptMapper paymentReceiptMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MonthlyDebtReportDetailService monthlyDebtReportDetailService;
    @Autowired
    private MonthlyInventoryReportDetailService monthlyInventoryReportDetailService;
    @Autowired
    private ParameterService parameterService;
    @Autowired
    private MonthlyDebtReportRepository monthlyDebtReportRepository;

    public PaymentReceiptResponse createPaymentReceipt(PaymentReceiptCreationRequest request) {
        Users user = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // ---------------- Validation ----------------
        boolean allowPartialPayment = true;
        try {
            allowPartialPayment = parameterService.getParamValue("allowPartialPayment") == 1.0;
        } catch (RuntimeException ignored) {
        }

        BigDecimal currentDebt = user.getDebtAmount() != null ? user.getDebtAmount() : BigDecimal.ZERO;

        // Payment greater than debt is not allowed
        if (request.getTotalAmount().compareTo(currentDebt) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_DEBT);
        }

        // Partial payment not allowed but amount less than debt
        if (!allowPartialPayment && request.getTotalAmount().compareTo(currentDebt) < 0) {
            throw new AppException(ErrorCode.PARTIAL_PAYMENT_NOT_ALLOWED);
        }
        // --------------------------------------------------

        PaymentReceipts paymentReceipt = paymentReceiptMapper.toPaymentReceipts(request);
        paymentReceipt.setCreateAt(LocalDate.now());
        monthlyDebtReportDetailService.createMonthlyDebtReportDetail(String.valueOf(user.getId()),
                request.getTotalAmount(), "Credit");
        user.setDebtAmount(currentDebt.subtract(request.getTotalAmount()));
        userRepository.save(user);

        return paymentReceiptMapper.toPaymentReceiptResponse(paymentReceiptRepository.save(paymentReceipt));
    }

    public List<PaymentReceiptResponse> getPaymentReceipts() {
        return paymentReceiptRepository.findAll().stream().map(paymentReceiptMapper::toPaymentReceiptResponse).toList();
    }

    public PaymentReceiptResponse getPaymentReceipt(Integer paymentReceiptId) {
        return paymentReceiptMapper.toPaymentReceiptResponse(
                paymentReceiptRepository.findById(paymentReceiptId)
                        .orElseThrow(() -> new RuntimeException("Payment receipt not found")));
    }

    public PaymentReceiptResponse updatePaymentReceipt(Integer paymentReceiptId, PaymentReceiptUpdateRequest request) {
        PaymentReceipts paymentReceipt = paymentReceiptRepository.findById(paymentReceiptId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_RECEIPT_NOT_EXISTED));
        paymentReceiptMapper.updatePaymentReceipt(paymentReceipt, request);
        return paymentReceiptMapper.toPaymentReceiptResponse(paymentReceiptRepository.save(paymentReceipt));
    }

    public void deletePaymentReceipt(Integer paymentReceiptId) {
        PaymentReceipts paymentReceipt = paymentReceiptRepository.findById(paymentReceiptId).orElse(null);
        if (paymentReceipt == null)
            return;

        log.info("Deleting payment receipt {} - reverting debt and updating reports", paymentReceiptId);

        Users user = userRepository.findById(paymentReceipt.getPayerId()).orElse(null);
        if (user != null && user.getDebtAmount() != null) {
            // Revert user debt amount
            BigDecimal newDebt = user.getDebtAmount().add(paymentReceipt.getTotalAmount());
            user.setDebtAmount(newDebt);
            userRepository.save(user);
            log.info("Reverted user {} debt by {}, new debt: {}",
                    user.getId(), paymentReceipt.getTotalAmount(), newDebt);

            // Use specific payment reversal method to properly reduce debtPayment
            monthlyDebtReportDetailService.reversePaymentDetail(String.valueOf(user.getId()),
                    paymentReceipt.getTotalAmount());
        }

        paymentReceiptRepository.delete(paymentReceipt);
        log.info("Successfully deleted payment receipt {} and reverted debt changes", paymentReceiptId);
    }
}
