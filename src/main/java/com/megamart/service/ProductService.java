package com.megamart.service;

import com.megamart.dto.product.ProductRequest;
import com.megamart.dto.product.ProductResponse;
import com.megamart.exception.BusinessException;
import com.megamart.exception.NotFoundException;
import com.megamart.model.Product;
import com.megamart.model.ProductImage;
import com.megamart.repository.ProductImageRepository;
import com.megamart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String category, Pageable pageable) {
        Page<Product> products = category == null || category.isBlank()
                ? productRepository.findByActiveTrue(pageable)
                : productRepository.findByActiveTrueAndCategoryIgnoreCase(category.trim(), pageable);
        return products.map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listOffers(Pageable pageable) {
        return productRepository.findByActiveTrueAndOfferPriceIsNotNull(pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findActiveCategories();
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new BusinessException("SKU is required.");
        }
        Product product = productRepository.findBySkuIgnoreCase(sku.trim())
                .filter(Product::isActive)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getForAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateOfferPrice(request);
        Product product = new Product(clean(request.sku()).toUpperCase(Locale.ROOT), clean(request.name()), clean(request.category()),
                request.price(), request.offerPrice(), cleanOptional(request.weight()), cleanOptional(request.heightPreference()),
                request.stockQuantity(), cleanOptional(request.imageUrl()));
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        validateOfferPrice(request);
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        product.update(clean(request.sku()).toUpperCase(Locale.ROOT), clean(request.name()), clean(request.category()),
                request.price(), request.offerPrice(), cleanOptional(request.weight()), cleanOptional(request.heightPreference()),
                request.stockQuantity(), cleanOptional(request.imageUrl()));
        return ProductResponse.from(product);
    }

    @Transactional
    public void saveImage(Long productId, String contentType, byte[] imageData) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        if (imageData == null || imageData.length == 0 || imageData.length > 5 * 1024 * 1024) {
            throw new BusinessException("Choose an image smaller than 5 MB.");
        }
        if (contentType == null || !Set.of("image/jpeg", "image/png", "image/webp", "image/gif").contains(contentType)) {
            throw new BusinessException("Choose a JPG, PNG, WEBP, or GIF image.");
        }
        productImageRepository.save(new ProductImage(productId, contentType, imageData));
        product.update(product.getSku(), product.getName(), product.getCategory(), product.getPrice(),
                product.getOfferPrice(),
                product.getWeight(), product.getHeightPreference(), product.getStockQuantity(),
                "/api/products/" + productId + "/image");
    }

    @Transactional(readOnly = true)
    public ProductImage image(Long productId) {
        return productImageRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product image not found."));
    }

    private String clean(String value) { return value.trim(); }
    private String cleanOptional(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private void validateOfferPrice(ProductRequest request) {
        if (request.offerPrice() != null
                && (request.offerPrice().signum() <= 0 || request.offerPrice().compareTo(request.price()) >= 0)) {
            throw new BusinessException("Offer price must be greater than zero and below the regular price.");
        }
    }
}
