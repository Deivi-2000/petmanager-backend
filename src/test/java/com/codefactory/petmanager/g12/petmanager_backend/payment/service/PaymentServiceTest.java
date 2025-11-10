package com.codefactory.petmanager.g12.petmanager_backend.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentRequestDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.PaymentsProductsDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.ProductDTO;
import com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto.ProductRequestDTO;
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

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentsProductsRepository paymentProductsRepository;

    @Mock
    private PaymentConditionRepository paymentConditionRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PaymentDTOBuilder paymentDTOBuilder;

    @InjectMocks
    private PaymentService paymentService;

    private Supplier testSupplier;
    private Payment testPayment;
    private Product testProduct;
    private PaymentsProducts testPaymentProduct;
    private PaymentRequestDTO testPaymentRequest;
    private PaymentResponseDTO testPaymentResponse;
    private ProductRequestDTO testProductRequest;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        testSupplier = new Supplier();
        testSupplier.setId(1);

        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBrand("Test Brand");

        testPayment = new Payment();
        testPayment.setId(1);
        testPayment.setSupplier(testSupplier);
        testPayment.setPaymentDate(LocalDate.now());
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setNotes("Test notes");

        testPaymentProduct = new PaymentsProducts();
        testPaymentProduct.setPayment(testPayment);
        testPaymentProduct.setProduct(testProduct);
        testPaymentProduct.setQuantity(2);
        testPaymentProduct.setPricePerUnit(new BigDecimal("50.00"));
        testPaymentProduct.setTotalAmount(new BigDecimal("100.00"));

        testProductDTO = new ProductDTO();
        testProductDTO.setId(1);
        testProductDTO.setName("Test Product");
        testProductDTO.setBrand("Test Brand");

        testProductRequest = new ProductRequestDTO();
        testProductRequest.setProduct(testProductDTO);
        testProductRequest.setQuantity(2);
        testProductRequest.setPricePerUnit(new BigDecimal("50.00"));

        testPaymentRequest = new PaymentRequestDTO();
        testPaymentRequest.setSupplierId(1);
        testPaymentRequest.setPaymentDate(LocalDate.now());
        testPaymentRequest.setNotes("Test notes");
        testPaymentRequest.setProducts(Arrays.asList(testProductRequest));

        testPaymentResponse = new PaymentResponseDTO();
        testPaymentResponse.setPaymentId(1);
        testPaymentResponse.setPaymentDate(LocalDate.now());
        testPaymentResponse.setAmount(new BigDecimal("100.00"));
        testPaymentResponse.setNotes("Test notes");
    }

    @Test
    void createPayment_Success() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(testSupplier));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(productMapper.productDTOToProduct(any(ProductDTO.class))).thenReturn(testProduct);
        when(productRepository.existsByNameIgnoreCaseAndBrandIgnoreCase(anyString(), anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(paymentMapper.paymentToPaymentResponseDTO(any(Payment.class))).thenReturn(testPaymentResponse);

        // Act
        PaymentResponseDTO result = paymentService.createPayment(testPaymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentResponse.getPaymentId(), result.getPaymentId());
        assertEquals(testPaymentResponse.getAmount(), result.getAmount());
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentProductsRepository).saveAll(anyList());
    }

    @Test
    void createPayment_SupplierNotFound() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(testPaymentRequest);
        });
    }

    @Test
    void getAllPaymentConditions_Success() {
        // Arrange
        PaymentCondition condition1 = new PaymentCondition();
        PaymentCondition condition2 = new PaymentCondition();
        List<PaymentCondition> expected = Arrays.asList(condition1, condition2);
        when(paymentConditionRepository.findAll()).thenReturn(expected);

        // Act
        List<PaymentCondition> result = paymentService.getAllPaymentConditions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paymentConditionRepository).findAll();
    }

    @Test
    void getAllPaymentsBySupplierId_Success() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(testSupplier));
        when(paymentRepository.findBySupplier(testSupplier)).thenReturn(Arrays.asList(testPayment));
        when(paymentProductsRepository.findByPayment(testPayment)).thenReturn(Arrays.asList(testPaymentProduct));
        when(productMapper.productToProductDTO(any(Product.class))).thenReturn(testProductDTO);

        // Act
        SupplierPaymentsResponseDTO result = paymentService.getAllPaymentsBySupplierId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPayments().size());
        assertEquals(1, result.getSupplierId());
        verify(supplierRepository).findById(1);
        verify(paymentRepository).findBySupplier(testSupplier);
    }

    @Test
    void getLastAndNextPaymentsBySupplierId_Success() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(testSupplier));
        when(paymentRepository.findTopBySupplierAndPaymentDateLessThanEqualOrderByPaymentDateDesc(any(), any()))
                .thenReturn(Optional.of(testPayment));
        when(paymentRepository.findTopBySupplierAndPaymentDateAfterOrderByPaymentDateAsc(any(), any()))
                .thenReturn(Optional.of(testPayment));
        when(paymentDTOBuilder.buildPaymentResponseDTO(testPayment)).thenReturn(testPaymentResponse);

        // Act
        SupplierLastNextPaymentsResponseDTO result = paymentService.getLastAndNextPaymentsBySupplierId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSupplierId());
        assertNotNull(result.getLast());
        assertNotNull(result.getNext());
        verify(supplierRepository).findById(1);
    }

    @Test
    void getLastAndNextPaymentsBySupplierId_NoPayments() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(testSupplier));
        when(paymentRepository.findTopBySupplierAndPaymentDateLessThanEqualOrderByPaymentDateDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(paymentRepository.findTopBySupplierAndPaymentDateAfterOrderByPaymentDateAsc(any(), any()))
                .thenReturn(Optional.empty());

        // Act
        SupplierLastNextPaymentsResponseDTO result = paymentService.getLastAndNextPaymentsBySupplierId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSupplierId());
        assertNull(result.getLast());
        assertNull(result.getNext());
        verify(supplierRepository).findById(1);
    }
}