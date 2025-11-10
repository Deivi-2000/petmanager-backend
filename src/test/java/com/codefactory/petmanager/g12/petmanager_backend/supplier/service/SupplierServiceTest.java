package com.codefactory.petmanager.g12.petmanager_backend.supplier.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.codefactory.petmanager.g12.petmanager_backend.payment.model.PaymentCondition;
import com.codefactory.petmanager.g12.petmanager_backend.payment.repository.PaymentConditionRepository;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.controller.dto.SupplierRequestDTO;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.controller.dto.SupplierResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.controller.dto.SupplierUpdateDTO;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.mapper.SupplierMapper;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.model.Supplier;
import com.codefactory.petmanager.g12.petmanager_backend.supplier.repository.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PaymentConditionRepository paymentConditionRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;
    private SupplierResponseDTO testSupplierResponse;
    private SupplierRequestDTO testSupplierRequest;
    private SupplierUpdateDTO testSupplierUpdate;
    private PaymentCondition testPaymentCondition;

    @BeforeEach
    void setUp() {
        testPaymentCondition = new PaymentCondition();
        testPaymentCondition.setId(1);
        testPaymentCondition.setName("Test Payment Condition");

        testSupplier = new Supplier();
        testSupplier.setId(1);
        testSupplier.setName("Test Supplier");
        testSupplier.setNit("123456789");
        testSupplier.setAddress("Test Address");
        testSupplier.setPhoneNumber("1234567890");
        testSupplier.setPaymentCondition(testPaymentCondition);
        testSupplier.setPaymentNotes("Test Payment Notes");

        testSupplierResponse = new SupplierResponseDTO();
        testSupplierResponse.setId(1);
        testSupplierResponse.setName("Test Supplier");
        testSupplierResponse.setNit("123456789");
        testSupplierResponse.setAddress("Test Address");
        testSupplierResponse.setPhoneNumber("1234567890");
        testSupplierResponse.setPaymentConditionId(1);
        testSupplierResponse.setPaymentNotes("Test Payment Notes");

        testSupplierRequest = new SupplierRequestDTO();
        testSupplierRequest.setName("Test Supplier");
        testSupplierRequest.setNit("123456789");
        testSupplierRequest.setAddress("Test Address");
        testSupplierRequest.setPhoneNumber("1234567890");
        testSupplierRequest.setPaymentConditionId(1);
        testSupplierRequest.setPaymentNotes("Test Payment Notes");

        testSupplierUpdate = new SupplierUpdateDTO();
        testSupplierUpdate.setName("Updated Supplier");
        testSupplierUpdate.setNit("987654321");
        testSupplierUpdate.setAddress("Updated Address");
        testSupplierUpdate.setPhoneNumber("0987654321");
        testSupplierUpdate.setPaymentConditionId(1);
        testSupplierUpdate.setPaymentNotes("Updated Payment Notes");
    }

    @Test
    void getSupplierById_Success() {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(testSupplier));
        when(supplierMapper.supplierToSupplierResponseDTO(testSupplier)).thenReturn(testSupplierResponse);

        SupplierResponseDTO result = supplierService.getSupplierById(1);

        assertNotNull(result);
        assertEquals(testSupplierResponse.getId(), result.getId());
        assertEquals(testSupplierResponse.getName(), result.getName());
        verify(supplierRepository).findById(1);
    }

    @Test
    void getSupplierById_NotFound() {
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            supplierService.getSupplierById(1);
        });
    }

    @Test
    void getSupplierByNit_Success() {
        when(supplierRepository.findByNit("123456789")).thenReturn(Optional.of(testSupplier));
        when(supplierMapper.supplierToSupplierResponseDTO(testSupplier)).thenReturn(testSupplierResponse);

        SupplierResponseDTO result = supplierService.getSupplierByNit("123456789");

        assertNotNull(result);
        assertEquals(testSupplierResponse.getNit(), result.getNit());
        verify(supplierRepository).findByNit("123456789");
    }

    @Test
    void getSupplierByNit_NotFound() {
        when(supplierRepository.findByNit("123456789")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            supplierService.getSupplierByNit("123456789");
        });
    }

    @Test
    void getAllSuppliers_Success() {
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        List<SupplierResponseDTO> expectedResponses = Arrays.asList(testSupplierResponse);

        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.suppliersToSupplierResponseDTOs(suppliers)).thenReturn(expectedResponses);

        List<SupplierResponseDTO> result = supplierService.getAllSuppliers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponses.get(0).getId(), result.get(0).getId());
        verify(supplierRepository).findAll();
    }

    @Test
    void searchSuppliers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(Arrays.asList(testSupplier));
        Page<SupplierResponseDTO> expectedPage = new PageImpl<>(Arrays.asList(testSupplierResponse));

        when(supplierRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(supplierPage);
        when(supplierMapper.supplierToSupplierResponseDTO(testSupplier)).thenReturn(testSupplierResponse);

        Page<SupplierResponseDTO> result = supplierService.searchSuppliers("Test", "123", "Address", 1, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedPage.getContent().get(0).getId(), result.getContent().get(0).getId());
        verify(supplierRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchSuppliersByName_Success() {
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        List<SupplierResponseDTO> expectedResponses = Arrays.asList(testSupplierResponse);

        when(supplierRepository.findByNameContainingIgnoreCase("Test")).thenReturn(suppliers);
        when(supplierMapper.suppliersToSupplierResponseDTOs(suppliers)).thenReturn(expectedResponses);

        List<SupplierResponseDTO> result = supplierService.searchSuppliersByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponses.get(0).getId(), result.get(0).getId());
        verify(supplierRepository).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void createSupplier_Success() {
        when(supplierRepository.existsByNit(anyString())).thenReturn(false);
        when(supplierRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(paymentConditionRepository.findById(1)).thenReturn(Optional.of(testPaymentCondition));
        when(supplierMapper.supplierRequestDTOToSupplier(testSupplierRequest)).thenReturn(testSupplier);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);
        when(supplierMapper.supplierToSupplierResponseDTO(testSupplier)).thenReturn(testSupplierResponse);

        SupplierResponseDTO result = supplierService.createSupplier(testSupplierRequest);

        assertNotNull(result);
        assertEquals(testSupplierResponse.getId(), result.getId());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void createSupplier_ExistingNit() {
        when(supplierRepository.existsByNit(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            supplierService.createSupplier(testSupplierRequest);
        });
    }

    @Test
    void createSupplier_ExistingName() {
        when(supplierRepository.existsByNit(anyString())).thenReturn(false);
        when(supplierRepository.findByName(anyString())).thenReturn(Optional.of(testSupplier));

        assertThrows(IllegalArgumentException.class, () -> {
            supplierService.createSupplier(testSupplierRequest);
        });
    }

    @Test
    void updateSupplier_Success() {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);
        when(supplierMapper.supplierToSupplierResponseDTO(testSupplier)).thenReturn(testSupplierResponse);
        when(paymentConditionRepository.findById(1)).thenReturn(Optional.of(testPaymentCondition));

        SupplierResponseDTO result = supplierService.updateSupplier(1, testSupplierUpdate);

        assertNotNull(result);
        assertEquals(testSupplierResponse.getId(), result.getId());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_NotFound() {
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            supplierService.updateSupplier(1, testSupplierUpdate);
        });
    }

    @Test
    void deleteSupplier_Success() {
        when(supplierRepository.existsById(1)).thenReturn(true);
        doNothing().when(supplierRepository).deleteById(1);

        assertDoesNotThrow(() -> supplierService.deleteSupplier(1));
        verify(supplierRepository).deleteById(1);
    }

    @Test
    void deleteSupplier_NotFound() {
        when(supplierRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            supplierService.deleteSupplier(1);
        });
    }
}