package cl.duoc.fullstack.review_service_m7.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productId,

        @NotNull(message = "El ID del usuario es obligatorio")
        Long userId,

        @NotNull(message = "La calificacion es obligatoria")
        @Min(value = 1, message = "La calificacion minima es 1 estrella")
        @Max(value = 5, message = "La calificacion maxima es 5 estrellas")
        Integer rating,

        @NotBlank(message = "El comentario es obligatorio")
        @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres")
        String comment
) {}
