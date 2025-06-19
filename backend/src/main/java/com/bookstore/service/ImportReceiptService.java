package com.bookstore.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.request.ImportReceiptCreationRequest;
import com.bookstore.dto.request.ImportReceiptUpdateRequest;
import com.bookstore.dto.response.ImportReceiptResponse;
import com.bookstore.entity.Books;
import com.bookstore.entity.BooksImportReceipts;
import com.bookstore.entity.ImportReceipts;
import com.bookstore.exception.AppException;
import com.bookstore.exception.ErrorCode;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.ImportReceiptMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.ImportReceiptRepository;
import com.bookstore.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImportReceiptService {
    @Autowired
    ImportReceiptRepository importReceiptRepository;
    ImportReceiptMapper importReceiptMapper;
    @Autowired
    private BookService bookService;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ParameterService parameterService;
    @Autowired
    MonthlyInventoryReportDetailService monthlyInventoryReportDetailService;

    @Transactional
    public ImportReceiptResponse createImportReceipt(ImportReceiptCreationRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String adminId;
        if (auth instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
            adminId = jwt.getClaim("id");
        } else {
            String username = auth.getName();
            adminId = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
                    .getId();
        }
        List<BookUpdateRequest> inputBooks = request.getBookDetails();
        int totalQuantity = inputBooks.stream().mapToInt(BookUpdateRequest::getQuantity).sum();
        Double paramValue = parameterService.getParamValue("minImportQuantity");
        Integer minImportQuantity = paramValue != null ? paramValue.intValue() : 0;
        if (totalQuantity < minImportQuantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_IMPORT_QUANTITY);
        }

        ImportReceipts importReceipt = importReceiptMapper.toImportReceipt(request);
        importReceipt.setImportDate(LocalDate.now());

        Set<BooksImportReceipts> booksImportReceiptsSet = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BookUpdateRequest inputBookRequest : inputBooks) {
            Books inputBook = bookRepository.findById(inputBookRequest.getBookId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

            // Update import price and sell price
            BigDecimal newImportPrice = inputBookRequest.getImportPrice();
            inputBook.setImportPrice(newImportPrice);
            inputBook.setSellPrice(newImportPrice.multiply(new BigDecimal("1.05")));
            bookRepository.save(inputBook);

            bookService.updateBook(inputBookRequest.getBookId(), inputBookRequest);

            BooksImportReceipts booksImportReceipt = new BooksImportReceipts();
            booksImportReceipt.setBook(inputBook);
            booksImportReceipt.setImportReceipt(importReceipt);
            BigDecimal lineAmount = newImportPrice
                    .multiply(BigDecimal.valueOf(inputBookRequest.getQuantity()));
            totalAmount = totalAmount.add(lineAmount);
            booksImportReceipt.setQuantity(inputBookRequest.getQuantity());
            booksImportReceipt.setImportPrice(newImportPrice);

            booksImportReceiptsSet.add(booksImportReceipt);
        }

        importReceipt.setTotalAmount(totalAmount);
        importReceipt.setBookDetails(booksImportReceiptsSet);
        importReceipt.setAdminId(adminId);

        ImportReceipts savedImportReceipt = importReceiptRepository.save(importReceipt);

        return importReceiptMapper.toImportReceiptResponse(savedImportReceipt);
    }

    public List<ImportReceiptResponse> getImportReceipts() {
        return importReceiptRepository.findAll().stream().map(importReceiptMapper::toImportReceiptResponse).toList();
    }

    public ImportReceiptResponse getImportReceipt(Integer importReceiptId) {
        return importReceiptMapper.toImportReceiptResponse(importReceiptRepository.findById(importReceiptId)
                .orElseThrow(() -> new RuntimeException("Import receipt not found")));
    }

    @Transactional
    public ImportReceiptResponse updateImportReceipt(Integer importRequestId, ImportReceiptUpdateRequest request) {
        ImportReceipts importReceipt = importReceiptRepository.findById(importRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.IMPORT_RECEIPT_NOT_EXISTED));

        log.info("Updating import receipt {} - handling inventory report changes", importRequestId);

        List<BookUpdateRequest> inputBooks = request.getBookDetails();
        int totalQuantity = inputBooks.stream().mapToInt(BookUpdateRequest::getQuantity).sum();

        if (totalQuantity < 150) {
            throw new AppException(ErrorCode.INSUFFICIENT_IMPORT_QUANTITY);
        }

        Map<Integer, BooksImportReceipts> existingBookDetailsMap = importReceipt.getBookDetails().stream()
                .collect(Collectors.toMap(
                        b -> b.getBook().getBookId(),
                        b -> b));

        Map<Integer, Integer> oldQuantityMap = importReceipt.getBookDetails().stream()
                .collect(Collectors.toMap(
                        b -> b.getBook().getBookId(),
                        BooksImportReceipts::getQuantity));

        Set<Integer> processedBookIds = new HashSet<>();

        for (BookUpdateRequest inputBookRequest : inputBooks) {
            int newQuantity = inputBookRequest.getQuantity();
            int oldQuantity = oldQuantityMap.getOrDefault(inputBookRequest.getBookId(), 0);
            int quantityDiff = newQuantity - oldQuantity;

            // Validate quantity changes won't cause negative stock
            Books bookToUpdate = bookRepository.findById(inputBookRequest.getBookId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

            if (quantityDiff < 0 && bookToUpdate.getQuantity() + quantityDiff < 0) {
                log.error("Cannot update import receipt {} - book {} would have negative quantity ({} + {} = {})",
                        importRequestId, inputBookRequest.getBookId(), bookToUpdate.getQuantity(), quantityDiff,
                        bookToUpdate.getQuantity() + quantityDiff);
                throw new AppException(ErrorCode.BOOK_QUANTITY_UNDER_LIMIT);
            }

            // Update prices in the book entity itself
            BigDecimal newImportPrice = inputBookRequest.getImportPrice();
            bookToUpdate.setImportPrice(newImportPrice);
            bookToUpdate.setSellPrice(newImportPrice.multiply(new BigDecimal("1.05")));
            bookRepository.save(bookToUpdate);

            // Handle inventory report changes
            if (quantityDiff != 0) {
                log.info("Book {} quantity change: {} -> {} (diff: {})",
                        inputBookRequest.getBookId(), oldQuantity, newQuantity, quantityDiff);

                // Create inventory report detail for the difference
                monthlyInventoryReportDetailService.createMonthlyInventoryReportDetail(
                        inputBookRequest.getBookId(), quantityDiff, "Import");
            }

            // Update book quantity directly (avoid double inventory reporting)
            if (quantityDiff != 0) {
                bookToUpdate.setQuantity(bookToUpdate.getQuantity() + quantityDiff);
                bookRepository.save(bookToUpdate);
            }

            BooksImportReceipts booksImportReceipt = existingBookDetailsMap.get(inputBookRequest.getBookId());

            if (booksImportReceipt != null) {
                booksImportReceipt.setQuantity(newQuantity);
                booksImportReceipt.setImportPrice(newImportPrice);
            } else {
                // New book added to receipt
                log.info("Adding new book {} with quantity {} to import receipt",
                        inputBookRequest.getBookId(), newQuantity);

                booksImportReceipt = new BooksImportReceipts();
                booksImportReceipt.setBook(bookToUpdate);
                booksImportReceipt.setImportReceipt(importReceipt);
                booksImportReceipt.setQuantity(newQuantity);
                booksImportReceipt.setImportPrice(newImportPrice);
                importReceipt.getBookDetails().add(booksImportReceipt);
            }

            processedBookIds.add(inputBookRequest.getBookId());
        }

        Set<BooksImportReceipts> itemsToRemove = importReceipt.getBookDetails().stream()
                .filter(detail -> !processedBookIds.contains(detail.getBook().getBookId()))
                .collect(Collectors.toSet());

        // Validate that removing items won't cause negative quantities
        for (BooksImportReceipts itemToRemove : itemsToRemove) {
            Books book = itemToRemove.getBook();
            int currentQuantity = book.getQuantity();
            int quantityToRemove = itemToRemove.getQuantity();

            if (currentQuantity < quantityToRemove) {
                log.error(
                        "Cannot update import receipt {} - removing book {} would cause negative quantity ({} - {} = {})",
                        importRequestId, book.getBookId(), currentQuantity, quantityToRemove,
                        currentQuantity - quantityToRemove);
                throw new AppException(ErrorCode.BOOK_QUANTITY_UNDER_LIMIT);
            }
        }

        for (BooksImportReceipts itemToRemove : itemsToRemove) {
            log.info("Removing book {} with quantity {} from import receipt",
                    itemToRemove.getBook().getBookId(), itemToRemove.getQuantity());

            // Update book quantity directly
            Books book = itemToRemove.getBook();
            book.setQuantity(book.getQuantity() - itemToRemove.getQuantity());
            bookRepository.save(book);

            // Create negative inventory report detail for removed book
            monthlyInventoryReportDetailService.createMonthlyInventoryReportDetail(
                    itemToRemove.getBook().getBookId(), -itemToRemove.getQuantity(), "Import");
        }
        importReceipt.getBookDetails().removeIf(detail -> !processedBookIds.contains(detail.getBook().getBookId()));
        BigDecimal totalAmount = importReceipt.getBookDetails().stream()
                .map(detail -> detail.getImportPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        importReceipt.setTotalAmount(totalAmount);

        ImportReceipts savedImportReceipt = importReceiptRepository.save(importReceipt);
        log.info("Successfully updated import receipt {} with inventory report changes", importRequestId);

        return importReceiptMapper.toImportReceiptResponse(savedImportReceipt);
    }

    @Transactional
    public void deleteImportReceipt(Integer importReceiptId) {
        ImportReceipts importReceipt = importReceiptRepository.findById(importReceiptId).orElse(null);
        if (importReceipt != null) {
            log.info("Deleting import receipt {} - reverting book quantities and inventory reports", importReceiptId);

            // Validate that reverting won't cause negative quantities
            for (BooksImportReceipts detail : importReceipt.getBookDetails()) {
                Books book = detail.getBook();
                int currentQuantity = book.getQuantity();
                int quantityToRevert = detail.getQuantity();

                if (currentQuantity < quantityToRevert) {
                    log.error("Cannot delete import receipt {} - book {} would have negative quantity ({} - {} = {})",
                            importReceiptId, book.getBookId(), currentQuantity, quantityToRevert,
                            currentQuantity - quantityToRevert);
                    throw new AppException(ErrorCode.BOOK_QUANTITY_UNDER_LIMIT);
                }
            }

            // Revert book quantities and create negative inventory report details
            for (BooksImportReceipts detail : importReceipt.getBookDetails()) {
                Books book = detail.getBook();
                int quantityToRevert = detail.getQuantity();

                // Revert book quantity
                book.setQuantity(book.getQuantity() - quantityToRevert);
                bookRepository.save(book);

                log.info("Reverted book {} quantity by {}, new quantity: {}",
                        book.getBookId(), quantityToRevert, book.getQuantity());

                // Create negative inventory report detail to offset the original import
                monthlyInventoryReportDetailService.createMonthlyInventoryReportDetail(
                        book.getBookId(), -quantityToRevert, "Import");
            }

            importReceiptRepository.delete(importReceipt);
            log.info("Successfully deleted import receipt {} and reverted all changes", importReceiptId);
        }
    }
}
