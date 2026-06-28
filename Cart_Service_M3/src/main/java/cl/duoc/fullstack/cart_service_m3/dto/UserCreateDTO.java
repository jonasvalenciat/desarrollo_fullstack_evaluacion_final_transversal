package cl.duoc.fullstack.cart_service_m3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateDTO(
        @NotBlank(message = "El nombre es requerido")
        String name,

        @NotBlank(message = "El email es requerido")
        @Email(message = "Formato de email invalido")
        String email
) {}
