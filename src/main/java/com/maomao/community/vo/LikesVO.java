package com.maomao.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author MaoJY
 * @create 2022-04-14 22:10
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikesVO {
    private int likeStatus;
    private long likeCount;
}