package com.codefactory.petmanager.g12.petmanager_backend.payment.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {

    @Valid
    @NotNull(message = "El producto es requerido")
    private ProductDTO product;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Integer quantity;

    @NotNull(message = "El precio por unidad es requerido")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio por unidad debe ser mayor que 0")
    private BigDecimal pricePerUnit;
}