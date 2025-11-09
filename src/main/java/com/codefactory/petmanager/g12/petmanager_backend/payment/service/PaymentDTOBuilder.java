package com.codefactory.petmanager.g12.petmanager_backend.payment.service;

import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.Payment;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentDTOBuilder {
    @Transactional(readOnly = true)
    PaymentResponseDTO buildPaymentResponseDTO(Payment payment);
}