package com.test.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.com.test.vo.ResultVO;
import com.test.entity.User;
import com.test.exception.ShopException;
import com.test.form.UserLoginForm;
import com.test.form.UserRegisterForm;
import com.test.result.ResponseEnum;
import com.test.service.UserService;
import com.test.util.JwtUtil;
import com.test.util.MD5Util;
import com.test.util.RegexValidateUtil;
import com.test.util.ResultVOUtil;
import com.test.vo.UserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author test
 * @since 2026-08-17
 */
@RestController
@RequestMapping("/user")



public class UserController {

    @Autowired
    private UserService userService;

    //用户注册
    @PostMapping("/register")
    public ResultVO register(@RequestBody UserRegisterForm registerForm) {

        //判断验证码是否正确

        //判断手机号格式是否正确
        String mobile = registerForm.getMobile();
        String password = registerForm.getPassword();
        Integer code = registerForm.getCode();
        if (mobile == null) throw new ShopException(ResponseEnum.MOBILE_NULL.getMsg());
        if (password == null) throw new ShopException(ResponseEnum.PASSWORD_NULL.getMsg());
        if (code == null) throw new ShopException(ResponseEnum.CODE_NULL.getMsg());
        boolean checkMobile = RegexValidateUtil.checkMobile(mobile);
        if (!checkMobile) throw new ShopException(ResponseEnum.MOBILE_ERROR.getMsg());

        //判断手机号是否已经被注册过
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        User user = this.userService.getOne(queryWrapper);
        if (user != null) throw new ShopException(ResponseEnum.MOBILE_EXIST.getMsg());
        User user1 = new User();


        String saltMD5= MD5Util.getSaltMD5(registerForm.getPassword());
        user1.setPassword(saltMD5);
        user1.setMobile(mobile);
        boolean save = this.userService.save(user1);
        if (save) return ResultVOUtil.success(null);
        return ResultVOUtil.fail(ResponseEnum.USER_REGISTER_ERROR.getMsg());


    }

    //用户登录
    @GetMapping("/login")
    public ResultVO login(UserLoginForm loginForm) {
        String mobile = loginForm.getMobile();
        String password = loginForm.getPassword();


        if (mobile == null) throw new ShopException(ResponseEnum.MOBILE_NULL.getMsg());
        if (password == null) throw new ShopException(ResponseEnum.PASSWORD_NULL.getMsg());
        boolean checkMobile = RegexValidateUtil.checkMobile(mobile);
        if (!checkMobile) throw new ShopException(ResponseEnum.MOBILE_ERROR.getMsg());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        User user = this.userService.getOne(queryWrapper);
        if (user == null) throw new ShopException(ResponseEnum.MOBILE_NOT_EXIST.getMsg());
        boolean saltverifyMD5 = MD5Util.getSaltverifyMD5(loginForm.getPassword(), user.getPassword());
        if (!saltverifyMD5) throw new ShopException(ResponseEnum.PASSWORD_ERROR.getMsg());
        String token = JwtUtil.createToken(user.getUserId(), user.getMobile());
        UserLoginVO vo = new UserLoginVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);
        return ResultVOUtil.success(vo);


    }
    //token验证
    @GetMapping("/checkToken/{token}")

    public ResultVO checkToken(@PathVariable("token") String token) {
        boolean checkToken = JwtUtil.checkToken(token);
        if (checkToken) return ResultVOUtil.success(checkToken);
        return ResultVOUtil.fail(ResponseEnum.USER_TOKEN_ERROR.getMsg());

    }




}

