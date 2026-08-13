package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 动态图片实体
 * 一条动态可以有多张图片，通过 moment_id 关联
 * 图片 URL 来自上传接口或 PicGo 图床
 */
@Data
@TableName("moment_image")
public class MomentImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long momentId;
    private String url;
    /** 排序序号，越小越靠前 */
    private Integer sort;
}
