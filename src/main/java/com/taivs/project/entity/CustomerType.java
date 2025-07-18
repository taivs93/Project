package com.taivs.project.entity;

public enum CustomerType {
    UY_TIN("Chua tung bom hang"),
    IT_BOM_HANG("It bom hang"),
    THUONG_XUYEN_BOM_HANG("Thuong xuyen bom hang"),
    RAT_HAY_BOM_HANG("Rat hay bom hang"),;

    private final String description;

    CustomerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
