package com.bookstore.mapper;

import com.bookstore.dto.response.MonthlyDebtReportResponse;
import com.bookstore.entity.MonthlyDebtReports;
import com.bookstore.entity.Users;
import com.bookstore.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class MonthlyDebtReportMapper {
    @Autowired
    protected UserRepository userRepository;

    @Mappings({
            @Mapping(target = "paidAmount", source = "debtPayment"),
            @Mapping(target = "userName", expression = "java(getUserName(monthlyDebtReports.getUserId()))"),
            @Mapping(target = "userId", source = "userId")
    })
    public abstract MonthlyDebtReportResponse toMonthlyDebtReportResponse(MonthlyDebtReports monthlyDebtReports);

    protected String getUserName(String userId) {
        if (userId == null)
            return null;
        return userRepository.findById(userId)
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse(null);
    }
}
