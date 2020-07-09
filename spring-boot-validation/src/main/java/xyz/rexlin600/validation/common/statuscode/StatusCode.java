package xyz.rexlin600.validation.common.statuscode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公共状态码
 *
 * @author rexlin600
 */
@Getter
@AllArgsConstructor
public enum StatusCode implements IStatusCode {

    /**
     * 操作成功
     */
    SUCCESS(1000, "操作成功", "操作成功"),
    /**
     * 参数错误
     */
    PARAM_ERROR(1005L, "参数错误", "参数错误"),
    /**
     * 业务错误
     */
    BUSINESS_ERROR(1006, "业务错误", "业务错误");

    /**
     * 状态码
     */
    private long code;

    /**
     * 描述信息
     */
    private String msg;

    private String errorMsg;
}