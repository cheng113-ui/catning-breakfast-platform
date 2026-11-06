package com.zhengli.catning.product;

import com.zhengli.catning.common.BusinessException;
import com.zhengli.catning.product.dto.ProductRequest;
import com.zhengli.catning.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(
                request.name().trim(),
                normalizeDescription(request.description()),
                request.price(),
                request.stock()
        );
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getRequired(id);
        product.update(
                request.name().trim(),
                normalizeDescription(request.description()),
                request.price(),
                request.stock()
        );
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse changeStatus(Long id, boolean active) {
        Product product = getRequired(id);
        product.changeActive(active);
        return ProductResponse.from(product);
    }

    private Product getRequired(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(HttpStatus.NOT_FOUND, "商品不存在")
                );
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
