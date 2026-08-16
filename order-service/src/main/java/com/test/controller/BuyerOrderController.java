package com.test.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.com.test.vo.ResultVO;
import com.test.entity.OrderDetail;
import com.test.entity.OrderMaster;
import com.test.entity.ProductInfo;
import com.test.exception.ShopException;
import com.test.feign.ProductFeign;
import com.test.form.BuyerOrderForm;
import com.test.form.BuyerOrderInnerForm;
import com.test.result.ResponseEnum;
import com.test.service.OrderDetailService;
import com.test.service.OrderMasterService;
import com.test.util.ResultVOUtil;
import com.test.vo.BuyerOrderDetailVO;
import com.test.vo.BuyerOrderMasterVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单详情表 前端控制器
 * </p>
 *
 * @author test
 * @since 2026-08-16
 */
@RestController
@RequestMapping("/buyer/order")
public class BuyerOrderController {

    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private OrderMasterService orderMasterService;

    @Autowired
    private OrderDetailService orderDetailService;


   @PostMapping("/create")
    public ResultVO create(@RequestBody BuyerOrderForm form) {
       //添加主表
       //计算订单总价
       List<BuyerOrderInnerForm> items = form.getItems();
       BigDecimal orderAmount = new BigDecimal("0");
       for (BuyerOrderInnerForm item : items) {
           Integer productId = item.getProductId();
           Integer productQuantity = item.getProductQuantity();
           //获取商品价格
           BigDecimal price = this.productFeign.findPriceById(productId);

           BigDecimal multiply = price.multiply(new BigDecimal(productQuantity));
           orderAmount = orderAmount.add(multiply);
       }

       OrderMaster orderMaster = new OrderMaster();
       orderMaster.setOrderAmount(orderAmount);
       orderMaster.setOrderStatus(0);
       orderMaster.setPayStatus(0);
       orderMaster.setBuyerName(form.getName());
       orderMaster.setBuyerPhone(form.getPhone());
       orderMaster.setBuyerOpenid(form.getId());
       orderMaster.setBuyerAddress(form.getAddress());
       boolean save = this.orderMasterService.save(orderMaster);
       //添加从表
       for (BuyerOrderInnerForm item : items) {
           //添加从表
           Integer id = item.getProductId();
           Integer productQuantity = item.getProductQuantity();
           OrderDetail orderDetail = new OrderDetail();
           orderDetail.setOrderId(orderMaster.getOrderId());
           ProductInfo productInfo = this.productFeign.findById(id);
           BeanUtils.copyProperties(productInfo, orderDetail);
           orderDetail.setProductQuantity(productQuantity);
           this.orderDetailService.save(orderDetail);
           //减库存
           boolean subStockById = this.productFeign.subStockById(id, productQuantity);

       }

       Map map = new HashMap<>();
       map.put("orderId", orderMaster.getOrderId());
       return ResultVOUtil.success(map);


   }

   //查询订单列表
   @GetMapping("/list/{buyerId}/{page}/{size}")
   public ResultVO List(
           @PathVariable("buyerId") String buyerId,
           @PathVariable("page") Integer page,
           @PathVariable("size") Integer size
   ) {
       QueryWrapper<OrderMaster> queryWrapper = new QueryWrapper<>();
       queryWrapper.eq("buyer_openid", buyerId);
       Page<OrderMaster> pageModel = new Page<>(page, size);
       Page<OrderMaster> resultPage = this.orderMasterService.page(pageModel, queryWrapper);
        return ResultVOUtil.success(resultPage.getRecords());
   }

   //查询订单详情
    @GetMapping("/detail/{buyerId}/{orderId}")
    public ResultVO detail(
            @PathVariable("buyerId") Integer buyerId,
            @PathVariable("orderId") String orderId
    ) {
        QueryWrapper<OrderMaster> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_openid", buyerId)
                .eq("order_id", orderId);
        OrderMaster orderMaster = this.orderMasterService.getOne(queryWrapper);
        if (orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        BuyerOrderMasterVO masterVO = new BuyerOrderMasterVO();
        BeanUtils.copyProperties(orderMaster,masterVO);
        QueryWrapper<OrderDetail> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("order_id", orderId);
        List<OrderDetail> orderDetailList = this.orderDetailService.list(queryWrapper1);

        List<BuyerOrderDetailVO> list = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            BuyerOrderDetailVO detailVO = new BuyerOrderDetailVO();
            BeanUtils.copyProperties(orderDetail, detailVO);
            list.add(detailVO);
        }
        masterVO.setOrderDetailList(list);
        return ResultVOUtil.success(masterVO);
    }

    //取消订单
    @PutMapping("/cancel/{buyerId}/{orderId}")
    public ResultVO cancel(
            @PathVariable("buyerId") Integer buyerId,
            @PathVariable("orderId") String orderId
    ) {
       QueryWrapper<OrderMaster> queryWrapper = new QueryWrapper<>();
       queryWrapper.eq("buyer_openid", buyerId);
       queryWrapper.eq("order_id", orderId);
       OrderMaster orderMaster = this.orderMasterService.getOne(queryWrapper);
       if (orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
       if (orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH.getMsg());
       if (orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL.getMsg());
       orderMaster.setOrderStatus(2);
       boolean updateById = this.orderMasterService.updateById(orderMaster);
       //恢复库存
        QueryWrapper<OrderDetail> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("order_id", orderId);
        List<OrderDetail> orderDetailList = this.orderDetailService.list(queryWrapper1);
        for (OrderDetail orderDetail : orderDetailList) {
            Integer productId = orderDetail.getProductId();
            Integer productQuantity = orderDetail.getProductQuantity();
            this.productFeign.subStockById(productId, -productQuantity);
        }
       return ResultVOUtil.success(null);
    }

    //完结订单
    @PutMapping("/finish/{orderId}")
    public ResultVO finish(@PathVariable("orderId") String orderId) {


       OrderMaster orderMaster = this.orderMasterService.getById(orderId);
       if (orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
       if (orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH_ERROR.getMsg());
       if (orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL_ERROR.getMsg());
       orderMaster.setOrderStatus(1);



       boolean updateById = this.orderMasterService.updateById(orderMaster);
       if (updateById) return ResultVOUtil.success(null);
       return ResultVOUtil.fail(ResponseEnum.ORDER_FINISH_FAIL.getMsg());

    }

    //支付订单
    @PutMapping("/pay/{orderId}/{buyerId}")
    public ResultVO pay(@PathVariable("orderId") String orderId,
                       @PathVariable("buyerId") String buyerId
    ) {
       QueryWrapper<OrderMaster> queryWrapper = new QueryWrapper<>();
       queryWrapper.eq("order_id", orderId);
       queryWrapper.eq("buyer_openid", buyerId);
       OrderMaster orderMaster = this.orderMasterService.getOne(queryWrapper);
       if (orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
       if (orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH_PAT_ERROR.getMsg());
       if (orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL_PAY_ERROR.getMsg());
       if (orderMaster.getPayStatus() == 1) throw new ShopException(ResponseEnum.ORDER_PAY_ERROR.getMsg());
       orderMaster.setPayStatus(1);
       boolean updateById = this.orderMasterService.updateById(orderMaster);
       if (updateById) return ResultVOUtil.success(null);
       return ResultVOUtil.fail(ResponseEnum.ORDER_PAY_FAIL.getMsg());

    }
}
