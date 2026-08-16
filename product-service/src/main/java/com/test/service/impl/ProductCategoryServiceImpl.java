package com.test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.com.test.vo.ResultVO;
import com.test.entity.ProductCategory;
import com.test.entity.ProductInfo;
import com.test.mapper.ProductCategoryMapper;
import com.test.mapper.ProductInfoMapper;
import com.test.service.ProductCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.vo.CategoryVO;
import com.test.vo.InfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 类目表 服务实现类
 * </p>
 *
 * @author test
 * @since 2026-08-13
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    @Autowired
    private ProductInfoMapper productInfoMapper;
    @Override
    public ResultVO categoryList() {
       List<ProductCategory> productCategoryList = this.productCategoryMapper.selectList(null);
       List<CategoryVO> categoryVOList = new ArrayList<>();
       for (ProductCategory productCategory : productCategoryList) {
           CategoryVO categoryVO = new CategoryVO();
           BeanUtils.copyProperties(productCategory,categoryVO);
           List<InfoVO> infoVOList = new ArrayList<>();
           QueryWrapper<ProductInfo> queryWrapper = new QueryWrapper<>();
           queryWrapper.eq("category_type", productCategory.getCategoryType());
           List<ProductInfo> productInfoList = this.productInfoMapper.selectList(queryWrapper);
           for(ProductInfo productInfo : productInfoList){
               InfoVO infoVO = new InfoVO();
               BeanUtils.copyProperties(productInfo,infoVO);
               infoVO.setQuantity(0);
               infoVOList.add(infoVO);
           }
           categoryVO.setGoods(infoVOList);
           categoryVOList.add(categoryVO);
       }
       ResultVO resultVO = new ResultVO();
        resultVO.setCode(0);
        resultVO.setMsg("success");
        resultVO.setData(categoryVOList);
       return resultVO;
    }
}
