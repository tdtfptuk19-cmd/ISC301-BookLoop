package com.bookloop.service;

import com.bookloop.dto.ReportForm;
import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.ReportRepository;
import com.bookloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user-submitted reports about listings or other users.
 */
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submit a report for a product listing.
     * Reporter cannot report their own listing.
     */
    public Report submitReport(ReportForm form, User reporter) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(form.getReason());
        report.setDescription(form.getDescription());
        report.setStatus(ReportStatus.PENDING);

        if (form.getProductId() != null) {
            Product product = productRepository.findById(form.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Listing not found."));

            if (product.getSeller().getId().equals(reporter.getId())) {
                throw new UnauthorizedOperationException("You cannot report your own listing.");
            }

            report.setReportedProduct(product);
            report.setReportedUser(product.getSeller());
        } else if (form.getReportedUserId() != null) {
            if (form.getReportedUserId().equals(reporter.getId())) {
                throw new UnauthorizedOperationException("You cannot report yourself.");
            }
            User reportedUser = userRepository.findById(form.getReportedUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            report.setReportedUser(reportedUser);
        }

        return reportRepository.save(report);
    }
}

