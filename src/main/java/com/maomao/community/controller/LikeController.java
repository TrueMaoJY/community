package com.maomao.community.controller;

import com.maomao.community.entity.Comment;
import com.maomao.community.entity.DiscussPost;
import com.maomao.community.entity.Event;
import com.maomao.community.entity.User;
import com.maomao.community.kafkaEvent.EventProducer;
import com.maomao.community.service.LikesService;
import com.maomao.community.util.HostHolder;
import com.maomao.community.util.RedisKeyUtil;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.LikesVO;
import com.maomao.community.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping("/like")
    @ResponseBody
    public RespBean likes(Integer entityType,Integer entityId,Integer entityUserId,int postId){
        User user = hostHolder.getUser();
        likesService.likes(entityType,entityId, user.getId(),entityUserId);
        long likeCount = likesService.getLikeCount(entityType, entityId);
        int likeStatus = likesService.findEntityLikeStatus(user.getId(),entityType, entityId);
        LikesVO likesVO = new LikesVO(likeStatus,likeCount);

        //发送消息 --点赞才需要发送
        if (likeStatus==1){
            Event event = new Event();
            event.setTopic(ConstantVO.TOPIC_LIKE)
                    .setEntityId(entityId)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setData("postId",postId)
                    .setEntityUserId(entityUserId);
            eventProducer.fireEvent(event);
            if(entityType==ConstantVO.ENTITY_TYPE_POST){
                //将帖子id存到redis中
                String redisKey = RedisKeyUtil.getPrefixPost();
                redisTemplate.opsForSet().add(redisKey,postId);
            }
        }

        return RespBean.success(likesVO);
    }
}