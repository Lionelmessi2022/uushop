package com.test.vo;

import lombok.Data;

@Data
public class UserLoginVO {
    private Integer userId;
    private String mobile;
    private String password;
    private String token;
}
