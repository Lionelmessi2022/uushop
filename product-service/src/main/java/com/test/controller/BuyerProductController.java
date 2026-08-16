package com.test.controller;


import com.test.com.test.vo.ResultVO;
import com.test.entity.ProductInfo;
import com.test.exception.ShopException;
import com.test.result.ResponseEnum;
import com.test.service.ProductCategoryService;
import com.test.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * <p>
 * 类目表 前端控制器
 * </p>
 *
 * @author test
 * @since 2026-08-13
 */
@RestController
@RequestMapping("/buyer/product")
public class BuyerProductController {
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ProductInfoService productInfoService;
    @GetMapping("/list")
    public ResultVO list(){
        return this.productCategoryService.categoryList();
    }
    @GetMapping("/findPriceById/{id}")
    public BigDecimal findPriceById(@PathVariable("id") Integer id){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        return productInfo.getProductPrice();
    }
    @GetMapping("/findById/{id}")
    public ProductInfo findById(@PathVariable("id") Integer id){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        return this.productInfoService.getById(id);
    }

    @PutMapping("/subStockById/{id}/{quantity}")
    public Boolean subStockById(@PathVariable("id") Integer id, @PathVariable("quantity") Integer quantity){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        Integer stock = productInfo.getProductStock();
        if(stock == 0) throw new ShopException(ResponseEnum. PRODUCT_STOCK_EMPTY.getMsg());
        Integer result = stock - quantity;
        if(result < 0) throw new ShopException(ResponseEnum. PRODUCT_STOCK_EMPTY.getMsg());
        productInfo.setProductStock(result);
        boolean updateById = this.productInfoService.updateById(productInfo);
        if(!updateById) throw new ShopException(ResponseEnum.PRODUCT_SUBSTOCK_ERROR.getMsg());
        return updateById;
    }



}

