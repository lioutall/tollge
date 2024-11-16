package com.tollge.common.auth;

import com.tollge.common.BaseModel;
import lombok.*;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class LoginUser extends BaseModel {
    /*
     * 用户id
     */
    private Long userId;
    /*
     * 昵称
     */
    private String nickname;
    /*
     * 头像
     */
    private String avatar;
    /*
     * 真实姓名
     */
    private String realname;
    /*
     * 手机
     */
    private String mobile;

    private Date loginTime;

    /**
     * 第一个是主角色
     */
    private List<Long> roleIdList;

}
