package com.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author test
 * @since 2026-08-17
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class User implements Serializable {

    private static final long serialVersionUID=1L;

      @TableId(value = "user_id", type = IdType.AUTO)
      private Integer userId;

    private String mobile;

    private String password;

      /**
     * 创建时间
     */
        @TableField(fill = FieldFill.INSERT)
      private Date createTime;

      /**
     * 修改时间
     */
        @TableField(fill = FieldFill.INSERT_UPDATE)
      private Date updateTime;


}
