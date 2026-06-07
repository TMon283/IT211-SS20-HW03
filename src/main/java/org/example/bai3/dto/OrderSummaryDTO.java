package org.example.bai3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderSummaryDTO {

    @JsonProperty("total_spent")
    private double totalSpent;

    @JsonProperty("purchased_items")
    private List<String> purchasedItems;
}
