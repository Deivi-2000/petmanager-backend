package com.codefactory.petmanager.g12.petmanager_backend.user.model;

import com.codefactory.petmanager.g12.petmanager_backend.auth.model.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "users",  uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_number", "id_type"})
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_number", nullable = false, length = 20)
    private String idNumber;

    @Column(name = "id_type", nullable = false, length = 3)
    private String idType;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // Método para spring security
    // devuelve según el método de autenticación, en este caso email
    public String getUsername() {
        return email;
    }
}