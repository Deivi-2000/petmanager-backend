package com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "El id del proveedor es requerido")
    private Integer supplierId;

    @NotNull(message = "La fecha de pago es requerida")
    @Future(message = "La fecha de pago debe ser posterior a la fecha actual")
    private LocalDate paymentDate;

    @NotEmpty(message = "La lista de productos no puede estar vacía")
    @Valid
    private List<ProductRequestDTO> products;

    private String notes;
}

