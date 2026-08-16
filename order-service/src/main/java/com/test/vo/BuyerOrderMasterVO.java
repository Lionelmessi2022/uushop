package com.test.vo;

import com.test.entity.OrderDetail;
import com.test.entity.OrderMaster;
import lombok.Data;

import java.util.List;

@Data
public class BuyerOrderMasterVO extends OrderMaster {
    private List<BuyerOrderDetailVO> orderDetailList;

}
