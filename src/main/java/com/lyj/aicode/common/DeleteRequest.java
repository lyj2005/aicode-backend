package com.lyj.aicode.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 根据id删除
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

