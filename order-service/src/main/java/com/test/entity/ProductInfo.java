package com.test.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInfo {

    private Integer productId;
    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品单价
     */
    private BigDecimal productPrice;

    /**
     * 小图
     */
    private String productIcon;

}
