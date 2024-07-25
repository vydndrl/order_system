package com.beyond.ordersystem.product.dto;


import com.beyond.ordersystem.product.domain.Product;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductSaveReqDto {
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private MultipartFile productImage;

    public Product toEntity() {
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .build();
    }
}
