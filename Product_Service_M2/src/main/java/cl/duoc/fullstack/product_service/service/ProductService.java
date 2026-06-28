package cl.duoc.fullstack.product_service.service;

import cl.duoc.fullstack.product_service.model.Product;
import cl.duoc.fullstack.product_service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        log.info("Obteniendo todos los productos");
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        log.info("Buscando producto con ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
    }

    public Product createProduct(Product product) {
        log.info("Creando producto: {}", product.getName());
        Product saved = productRepository.save(product);
        log.info("Producto creado exitosamente con ID: {}", saved.getId());
        return saved;
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        Product updated = productRepository.save(existing);
        log.info("Producto actualizado exitosamente con ID: {}", updated.getId());
        return updated;
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Producto no encontrado con id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Producto eliminado con ID: {}", id);
    }
}
