package com.test.service;

import com.test.com.test.vo.ResultVO;
import com.test.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 类目表 服务类
 * </p>
 *
 * @author test
 * @since 2026-08-13
 */
public interface ProductCategoryService extends IService<ProductCategory> {
public ResultVO categoryList();

}
