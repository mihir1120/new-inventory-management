package com.ashokvatika.api.product;

import com.ashokvatika.api.config.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        UUID tenantId = TenantContext.getTenantId();
        return productRepository.findAllByTenantIdOrderByNameAsc(tenantId)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productRepository.save(new Product(
                request.name(),
                request.price(),
                request.stockQuantity()
        ));
        return ProductResponse.from(product);
    }

    public record CreateProductRequest(
            @NotBlank String name,
            @NotNull @PositiveOrZero BigDecimal price,
            @PositiveOrZero int stockQuantity
    ) {
    }

    public record ProductResponse(
            UUID id,
            String name,
            BigDecimal price,
            int stockQuantity
    ) {
        static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getStockQuantity()
            );
        }
    }
}
