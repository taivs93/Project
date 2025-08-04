package com.taivs.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
public class ProductFullResponse {
    private Long id;
    private String name;
    private String barcode;
    private double weight;
    private double height;
    private double length;
    private double width;
    private double price;
    private int stock;
    private List<String> imageUrls;
}
