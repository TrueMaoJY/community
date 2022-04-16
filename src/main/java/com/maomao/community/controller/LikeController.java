package com.maomao.community.controller;

import com.maomao.community.entity.User;
import com.maomao.community.service.LikesService;
import com.maomao.community.util.HostHolder;
import com.maomao.community.vo.LikesVO;
import com.maomao.community.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author MaoJY
 * @create 2022-04-14 22:07
 * @Description:
 */
@Controller
public class LikeController {
    @Autowired
   private LikesService likesService;
    @Autowired
    private HostHolder hostHolder;
    @RequestMapping("/like")
    @ResponseBody
    public RespBean likes(Integer entityType,Integer entityId,Integer entityUserId){
        User user = hostHolder.getUser();
        likesService.likes(entityType,entityId, user.getId(),entityUserId);
        long likeCount = likesService.getLikeCount(entityType, entityId);
        int likeStatus = likesService.findEntityLikeStatus(user.getId(),entityType, entityId);
        LikesVO likesVO = new LikesVO(likeStatus,likeCount);
        return RespBean.success(likesVO);
    }
}