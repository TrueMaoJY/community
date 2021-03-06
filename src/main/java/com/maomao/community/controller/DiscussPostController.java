package com.maomao.community.controller;

import com.maomao.community.entity.*;
import com.maomao.community.kafkaEvent.EventProducer;
import com.maomao.community.service.CommentService;
import com.maomao.community.service.DiscussPostService;
import com.maomao.community.service.LikesService;
import com.maomao.community.service.UserService;
import com.maomao.community.util.HostHolder;
import com.maomao.community.util.RedisKeyUtil;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.RespBean;
import com.maomao.community.vo.RespBeanEnum;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author MaoJY
 * @create 2022-04-05 21:56
 * @Description:
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikesService likesService;
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/addPost")
    @ResponseBody
    @Transactional
    public RespBean addPost(DiscussPost discussPost){
        User user = hostHolder.getUser();
        if (user== null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        if (discussPost == null) {
            return RespBean.error(RespBeanEnum.TITLE_CONTENT_NULL);
        }
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);
        //??????????????????
        Event event =new Event()
                .setUserId(user.getId())
                .setEntityType(ConstantVO.ENTITY_TYPE_POST)
                .setTopic(ConstantVO.TOPIC_PUBLISH)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);
        //?????????id??????redis???
        String redisKey = RedisKeyUtil.getPrefixPost();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());
        return RespBean.success(RespBeanEnum.SUCCESS_ISSUE);
    }
//    @RequestMapping("/detail/{id}")
//    public String discussPostDetail(@PathVariable("id") int id , Model model){
//        DiscussPost discussPost = discussPostService.discussPostDetail(id);
//        model.addAttribute("post",discussPost);
//        User user = userService.findUserById(discussPost.getUserId());
//        model.addAttribute("user",user);
//        return "/site/discuss-detail";
//    }
    @RequestMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // ??????
        DiscussPost post = discussPostService.discussPostDetail(discussPostId);
        model.addAttribute("post", post);
        // ??????
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //??????
        long likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_POST, post.getId());
        int likeStatus=0;
        if(hostHolder.getUser()!=null){
           likeStatus= likesService.findEntityLikeStatus(hostHolder.getUser().getId(),
                    ConstantVO.ENTITY_TYPE_POST, post.getId());
        }
        int totalReplyCount = post.getCommentCount();
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);

        // ??????????????????
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(totalReplyCount);

        // ??????: ??????????????????
        // ??????: ??????????????????
        // ????????????
        List<Comment> commentList = commentService.findCommentsByEntity(
               ConstantVO.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // ??????VO??????
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // ??????VO
                Map<String, Object> commentVo = new HashMap<>();
                // ??????
                commentVo.put("comment", comment);
                // ??????
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //??????
                likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_COMMENT, comment.getId());
                likeStatus=0;
                if(hostHolder.getUser()!=null){
                    likeStatus= likesService.findEntityLikeStatus(hostHolder.getUser().getId(),
                            ConstantVO.ENTITY_TYPE_COMMENT, comment.getId());
                }
               commentVo.put("likeCount",likeCount);
                commentVo.put("likeStatus",likeStatus);
                // ????????????
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ConstantVO.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // ??????VO??????
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // ??????
                        replyVo.put("reply", reply);
                        // ??????
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //??????
                        likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_COMMENT, reply.getId());
                        likeStatus=0;
                        if(hostHolder.getUser()!=null){
                            likeStatus= likesService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                    ConstantVO.ENTITY_TYPE_COMMENT, reply.getId());
                        }
                        replyVo.put("likeCount",likeCount);
                        replyVo.put("likeStatus",likeStatus);

                        // ????????????
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // ????????????
                int replyCount = commentService.findCommentCount(ConstantVO.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
    /**
    * Description:??????
    * date: 2022/4/28 20:46
    * @author: MaoJY
    * @since JDK 1.8
    */
    //TODO ?????????????????????
    @RequestMapping("/top")
    @ResponseBody
    public RespBean top(int id){
        discussPostService.updateType(id,ConstantVO.POST_TYPE_TOP);
        //??????es????????????
        //??????????????????
        Event event =new Event()
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(ConstantVO.ENTITY_TYPE_POST)
                .setTopic(ConstantVO.TOPIC_PUBLISH)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return RespBean.success();
    }
    /**
    * Description:??????
    * date: 2022/4/28 20:53
    * @author: MaoJY
    * @since JDK 1.8
    */
    @RequestMapping("/wonderful")
    @ResponseBody
    public RespBean wonderful(int id){
        discussPostService.updateStatus(id,ConstantVO.POST_STATUS_WONDERFUL);
        //??????es????????????
        //??????????????????
        Event event =new Event()
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(ConstantVO.ENTITY_TYPE_POST)
                .setTopic(ConstantVO.TOPIC_PUBLISH)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        //?????????id??????redis???
        String redisKey = RedisKeyUtil.getPrefixPost();
        redisTemplate.opsForSet().add(redisKey,id);
        return RespBean.success();
    }
    /**
    * Description:??????
    * date: 2022/4/28 20:54
    * @author: MaoJY
    * @since JDK 1.8
    */
    @RequestMapping("/block")
    @ResponseBody
    public RespBean block(int id){
        discussPostService.updateStatus(id,ConstantVO.POST_STATUS_DELETE);
        //??????es????????????
        //????????????
        Event event =new Event()
                .setUserId(HostHolder.getUser().getId())
                .setEntityType(ConstantVO.ENTITY_TYPE_POST)
                .setTopic(ConstantVO.TOPIC_BLOCK)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return RespBean.success();
    }
}