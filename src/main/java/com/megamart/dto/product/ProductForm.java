package com.megamart.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductForm {
    @NotBlank @Size(max = 80)
    private String sku;
    @NotBlank @Size(max = 160)
    private String name;
    @NotBlank @Size(max = 100)
    private String category;
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
    private BigDecimal price;
    @Size(max = 80)
    private String weight;
    @Size(max = 80)
    private String heightPreference;
    @NotNull @Min(0)
    private Integer stockQuantity;
    @Size(max = 2048)
    private String imageUrl;

    public ProductForm() { }

    public ProductForm(ProductResponse product) {
        this.sku = product.sku();
        this.name = product.name();
        this.category = product.category();
        this.price = product.price();
        this.weight = product.weight();
        this.heightPreference = product.heightPreference();
        this.stockQuantity = product.stockQuantity();
        this.imageUrl = product.imageUrl();
    }

    public ProductRequest toRequest() {
        return new ProductRequest(sku, name, category, price, weight, heightPreference, stockQuantity, imageUrl);
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getHeightPreference() { return heightPreference; }
    public void setHeightPreference(String heightPreference) { this.heightPreference = heightPreference; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
