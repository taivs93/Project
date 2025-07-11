package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @PositiveOrZero(message = "Weight must be zero or positive")
    private double weight;

    @PositiveOrZero(message = "Height must be zero or positive")
    private double height;

    @PositiveOrZero(message = "Length must be zero or positive")
    private double length;

    @PositiveOrZero(message = "Width must be zero or positive")
    private double width;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private int stock;

    @PositiveOrZero(message = "Price must be zero or positive")
    private double price;
}
