package com.project.souklab.controller.subscription;

import com.project.souklab.dto.subscription.FinancialReasonRequest;
import com.project.souklab.service.subscription.AdminRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("@accessControl.canManageFinancialOperations(authentication)")
public class AdminRefundController {
    private final AdminRefundService refundService;

    @PostMapping("/{id}/refund")
    public void rejectRefund(@PathVariable String id, @Valid @RequestBody FinancialReasonRequest request) {
        refundService.reject(id, request);
    }
}
