package com.maomao.community.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author MaoJY
 * @create 2022-03-04 21:22
 * @Description:
 */
@ToString
@Getter
@AllArgsConstructor
public enum RespBeanEnum {
    SUCCESS (200,"success"),
    ERROR(500,"服务器端异常"),

    //登录

    LOGIN_ERROR (500200,"用户名或密码错误"),
    MOBILE_PATTERN_ERROR(500201,"手机号格式错误"),
    BIND_ERROR (500202,"数据校验异常"),
    SESSION_ERROR(500203,"用户信息已过期，请重新登录"),
    NOT_LOGIN(500204,"用户未登录"),
    AUTHORITY_DENIED(500205,"权限不够，无法访问"),

    //评论
    SUCCESS_ISSUE(500300,"发表评论成功"),
    TITLE_CONTENT_NULL(500301,"标题或者内容不能为空"),
    TOUSER_NOT_EXIST(500302,"接受用户不存在"),
    ;
    private final Integer code;
    private final  String msg;
}
