package cl.duoc.fullstack.review_service_m7.controller;

import cl.duoc.fullstack.review_service_m7.dto.ReviewRequest;
import cl.duoc.fullstack.review_service_m7.dto.ReviewResponse;
import cl.duoc.fullstack.review_service_m7.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Resenas", description = "API para gestionar resenas de productos")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Crear una resena", description = "Registra una nueva resena para un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resena creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse created = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una resena", description = "Actualiza una resena existente por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resena actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "404", description = "Resena no encontrada")
    })
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        ReviewResponse updated = reviewService.updateReview(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Obtener resenas por producto", description = "Retorna todas las resenas de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de resenas obtenida exitosamente")
    })
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(reviews);
    }
}
