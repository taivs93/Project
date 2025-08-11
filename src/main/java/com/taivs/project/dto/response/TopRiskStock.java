package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface TopRiskStock {
    @JsonProperty("id")
    Long getId();

    @JsonProperty("name")
    String getName();

    @JsonProperty("stock_qty")
    Integer getStockQty();

    @JsonProperty("avg_sales_per_day")
    Double getAvgSalesPerDay();

    @JsonProperty("stock_value")
    Double getStockValue();

    @JsonProperty("max_stock_value")
    Double getMaxStockValue();

    @JsonProperty("inventory_risk_score")
    Double getInventoryRiskScore();

    @JsonProperty("days_to_sell_out")
    Double getDaysToSellOut();

    @JsonProperty("stock_value_percentage")
    Double getStockValuePercentage();

    @JsonProperty("total_sales")
    Integer getTotalSales();

    @JsonProperty("sales_days")
    Integer getSalesDays();
}