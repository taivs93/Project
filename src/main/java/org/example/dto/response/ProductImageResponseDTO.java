package org.example.dto.response;

import lombok.*;
import org.example.entity.ProductImage;

@Getter
@Builder
public class ProductImageResponseDTO {
    private Long id;
    private String imageUrl;
    private Long productId;

    public static ProductImageResponseDTO fromEntity(ProductImage image) {
        return ProductImageResponseDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .productId(image.getProduct().getId())
                .build();
    }
}
