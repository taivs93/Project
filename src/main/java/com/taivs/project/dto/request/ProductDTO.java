package com.taivs.project.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProductDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 250, message = "Name must be at most 250 characters")
    private String name;

    @NotBlank(message = "Barcode is required")
    @Size(max = 50, message = "Barcode must be at most 50 characters")
    private String barcode;

    @Positive(message = "Weight must be greater than 0")
    private Double weight;

    @Positive(message = "Height must be greater than 0")
    private Double height;

    @Positive(message = "Length must be greater than 0")
    private Double length;

    @Positive(message = "Width must be greater than 0")
    private Double width;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock;

    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;
}
