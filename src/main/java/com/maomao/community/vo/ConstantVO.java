package com.maomao.community.vo;

/**
 * @author MaoJY
 * @create 2022-04-15 21:46
 * @Description:
 */
public class ConstantVO {
    public static final int ENTITY_TYPE_POST=1;//帖子
    public static final int ENTITY_TYPE_COMMENT=2;//评论
    public static final int ENTITY_TYPE_USER=3;//用户

    public static final String TOPIC_COMMENT="comment";
    public static final String TOPIC_LIKE="like";
    public static final String TOPIC_FOLLOW="follow";
    public static final String TOPIC_PUBLISH="publish";
    public static final String TOPIC_BLOCK="block";

    public static final int SYSTEM_USER_ID=1;

    /**
     * 激活成功
     */
   public static final int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static final int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    public static final int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    public static final int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
/**
* Description:用户权限
* date: 2022/4/27 18:54
* @author: MaoJY
* @since JDK 1.8
*/
    public static final String AUTHORITY_USER ="user";
    public static final String AUTHORITY_ADMIN ="admin";
    public static final String AUTHORITY_MODERATOR ="moderator";


/**
* Description:置顶，加精，删除
* date: 2022/4/28 20:48
* @author: MaoJY
* @since JDK 1.8
*/
    public static final int POST_TYPE_NORMAL=0;
    public static final int POST_TYPE_TOP=1;
    public static final int POST_STATUS_NORMAL=0;
    public static final int POST_STATUS_WONDERFUL=1;
    public static final int POST_STATUS_DELETE=2;
}