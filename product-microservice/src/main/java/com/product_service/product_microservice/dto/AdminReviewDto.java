package com.product_service.product_microservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminReviewDto {

    @NotBlank(message = "Action is required: APPROVED or REJECTED")
    private String action;

    private String remarks;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
