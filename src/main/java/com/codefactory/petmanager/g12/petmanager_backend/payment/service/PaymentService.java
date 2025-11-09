package com.codefactory.petmanager.g12.petmanager_backend.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentRequestDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentsProductsDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.SupplierLastNextPaymentsResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.SupplierPaymentsResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.mapper.PaymentMapper;
import com.codefactory.petmanager.g12.petmanager_backend.payment.mapper.ProductMapper;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.Payment;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.PaymentCondition;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.PaymentsProducts;
import com.codefactory.petmanager.g12.petmanager_backend.payment.model.Product;
import com.codefactory.petmanager.g12.petmanager_backend.payment.repository.PaymentConditionRepository;
import com.codefactory.petmanager.g12.petmanager_backend.payment.repository.PaymentRepository;
import com.codefactory.petmanager.g12.petmanager_backend.payment.repository.PaymentsProductsRepository;
import com.codefactory.petmanager.g12.petmanager_backend.payment.repository.ProductRepository;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.model.Supplier;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String SUPPLIER_NOT_FOUND_MESSAGE = "No existe un proveedor con id: ";

    private final PaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PaymentsProductsRepository paymentProductsRepository;
    private final PaymentConditionRepository paymentConditionRepository;
    private final PaymentMapper paymentMapper;
    private final ProductMapper productMapper;

    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO) {
        Supplier supplier = supplierRepository.findById(paymentRequestDTO.getSupplierId())
                        .orElseThrow(() -> new IllegalArgumentException(SUPPLIER_NOT_FOUND_MESSAGE + paymentRequestDTO.getSupplierId()));

        Payment payment = new Payment();
        payment.setSupplier(supplier);
        payment.setPaymentDate(paymentRequestDTO.getPaymentDate());
        payment.setNotes(paymentRequestDTO.getNotes());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var productDTO : paymentRequestDTO.getProducts()) {
            BigDecimal subtotal = productDTO.getPricePerUnit()
                            .multiply(BigDecimal.valueOf(productDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }
        payment.setAmount(totalAmount);
        Payment savedPayment = paymentRepository.save(payment);

        List<PaymentsProducts> paymentProductsList = new ArrayList<>();

        for (var productDTO : paymentRequestDTO.getProducts()) {
            Product mappedProduct = productMapper.productDTOToProduct(productDTO.getProduct());
            Product savedProduct = null;
            // Comprobar si el producto ya existe en la bd o crear uno nuevo
            if (productRepository.existsByNameIgnoreCaseAndBrandIgnoreCase(mappedProduct.getName(), mappedProduct.getBrand())) {
                savedProduct = productRepository.findByNameIgnoreCaseAndBrandIgnoreCase(mappedProduct.getName(), mappedProduct.getBrand());
            } else {
                savedProduct = productRepository.save(mappedProduct);
            }

            PaymentsProducts paymentProduct = new PaymentsProducts();
            paymentProduct.setPayment(savedPayment);
            paymentProduct.setProduct(savedProduct);
            paymentProduct.setQuantity(productDTO.getQuantity());
            paymentProduct.setPricePerUnit(productDTO.getPricePerUnit());
            paymentProduct.setTotalAmount(productDTO.getPricePerUnit()
                            .multiply(BigDecimal.valueOf(productDTO.getQuantity())));
            paymentProductsList.add(paymentProduct);
        }

        paymentProductsRepository.saveAll(paymentProductsList);

        return paymentMapper.paymentToPaymentResponseDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentCondition> getAllPaymentConditions() {
        List<PaymentCondition> paymentConditions = paymentConditionRepository.findAll();
        return paymentConditions;
    }

    @Transactional(readOnly = true)
    public SupplierPaymentsResponseDTO getAllPaymentsBySupplierId(int supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                        .orElseThrow(() -> new IllegalArgumentException(SUPPLIER_NOT_FOUND_MESSAGE + supplierId));
        
        List<Payment> supplierPayments = paymentRepository.findBySupplier(supplier);

        List<PaymentResponseDTO> paymentsResponses = new ArrayList<>();

        for (Payment payment : supplierPayments) {
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

            paymentsResponses.add(paymentResponse);
        }

        SupplierPaymentsResponseDTO response = new SupplierPaymentsResponseDTO();
        response.setSupplierId(supplierId);
        response.setPayments(paymentsResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public SupplierLastNextPaymentsResponseDTO getLastAndNextPaymentsBySupplierId(int supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException(SUPPLIER_NOT_FOUND_MESSAGE + supplierId));

        LocalDate today = LocalDate.now();

        Payment lastPayment = paymentRepository
                .findTopBySupplierAndPaymentDateLessThanEqualOrderByPaymentDateDesc(supplier, today)
                .orElse(null);

        Payment nextPayment = paymentRepository
                .findTopBySupplierAndPaymentDateAfterOrderByPaymentDateAsc(supplier, today)
                .orElse(null);

        PaymentResponseDTO lastPaymentDTO = lastPayment != null ? buildPaymentResponseDTO(lastPayment) : null;
        PaymentResponseDTO nextPaymentDTO = nextPayment != null ? buildPaymentResponseDTO(nextPayment) : null;

        SupplierLastNextPaymentsResponseDTO response = new SupplierLastNextPaymentsResponseDTO();
        response.setSupplierId(supplierId);
        response.setLast(lastPaymentDTO);
        response.setNext(nextPaymentDTO);

        return response;
    }

    @Transactional(readOnly = true)
    private PaymentResponseDTO buildPaymentResponseDTO(Payment payment) {
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