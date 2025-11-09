package com.codefactory.petmanager.g12.petmanager_backend.payment.service;

import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentsProductsDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.mapper.ProductMapper;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.Payment;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.PaymentsProducts;
import com.codefactory.petmanager.g12.petmanager_backend.payment.repository.PaymentsProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentDTOBuilderImpl implements PaymentDTOBuilder {

    private final PaymentsProductsRepository paymentProductsRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO buildPaymentResponseDTO(Payment payment) {
        List<PaymentsProducts> paymentsProducts = paymentProductsRepository.findByPayment(payment);

        List<PaymentsProductsDTO> productDTOs = new ArrayList<>();
        for (PaymentsProducts pp : paymentsProducts) {
            PaymentsProductsDTO dto = new PaymentsProductsDTO();
            dto.setProduct(productMapper.productToProductDTO(pp.getProduct()));
            dto.setQuantity(pp.getQuantity());
            dto.setPricePerUnit(pp.getPricePerUnit());
            productDTOs.add(dto);
        }

        PaymentResponseDTO paymentResponse = new PaymentResponseDTO();
        paymentResponse.setPaymentId(payment.getId());
        paymentResponse.setPaymentDate(payment.getPaymentDate());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setNotes(payment.getNotes());
        paymentResponse.setProducts(productDTOs);

        return paymentResponse;
    }
}