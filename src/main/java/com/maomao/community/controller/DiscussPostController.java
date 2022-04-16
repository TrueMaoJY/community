package com.maomao.community.controller;

import com.maomao.community.entity.Comment;
import com.maomao.community.entity.DiscussPost;
import com.maomao.community.entity.Page;
import com.maomao.community.entity.User;
import com.maomao.community.service.CommentService;
import com.maomao.community.service.DiscussPostService;
import com.maomao.community.service.LikesService;
import com.maomao.community.service.UserService;
import com.maomao.community.util.HostHolder;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.RespBean;
import com.maomao.community.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @RequestMapping("/addPost")
    @ResponseBody
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
        // 帖子
        DiscussPost post = discussPostService.discussPostDetail(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞
        long likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_POST, post.getId());
        int likeStatus=0;
        if(hostHolder.getUser()!=null){
           likeStatus= likesService.findEntityLikeStatus(hostHolder.getUser().getId(),
                    ConstantVO.ENTITY_TYPE_POST, post.getId());
        }
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
               ConstantVO.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //点赞
                likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_COMMENT, comment.getId());
                likeStatus=0;
                if(hostHolder.getUser()!=null){
                    likeStatus= likesService.findEntityLikeStatus(hostHolder.getUser().getId(),
                            ConstantVO.ENTITY_TYPE_COMMENT, comment.getId());
                }
               commentVo.put("likeCount",likeCount);
                commentVo.put("likeStatus",likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ConstantVO.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //点赞
                        likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_COMMENT, reply.getId());
                        likeStatus=0;
                        if(hostHolder.getUser()!=null){
                            likeStatus= likesService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                    ConstantVO.ENTITY_TYPE_COMMENT, reply.getId());
                        }
                        replyVo.put("likeCount",likeCount);
                        replyVo.put("likeStatus",likeStatus);

                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ConstantVO.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
}