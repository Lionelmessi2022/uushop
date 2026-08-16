package com.test.util;

import com.test.com.test.vo.ResultVO;

public class ResultVOUtil {

    public static ResultVO success(Object date){
        ResultVO resultVO = new ResultVO();
        resultVO.setData(date);
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        return resultVO;
    }

    public static ResultVO fail(String message) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(-1);
        resultVO.setMsg(message);
        return resultVO;
    }
}
