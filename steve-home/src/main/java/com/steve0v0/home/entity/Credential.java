package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credential")
public class Credential {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String secretHash;
    private Integer tokenVersion;
    private LocalDateTime updatedAt;
}
