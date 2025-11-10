package com.codefactory.petmanager.g12.petmanager_backend.supplier.controller.dto;

import com.codefactory.petmanager.g12.petmanager_backend.payment.model.PaymentCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponseDTO {
    
  private int id;
  private String nit;
  private String name;
  private String phoneNumber;
  private String address;
  private String email;
  private Integer paymentConditionId;
  private String paymentNotes;
  
}