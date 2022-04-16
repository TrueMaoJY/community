package com.maomao.community.service;

import com.maomao.community.dao.CommentMapper;
import com.maomao.community.entity.Comment;
import com.maomao.community.filter.SensitiveWordFilter;
import com.maomao.community.vo.ConstantVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author MaoJY
 * @create 2022-04-06 18:56
 * @Description:
 */
@Service
public class CommentService {
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    SensitiveWordFilter sensitiveWordFilter;
    @Autowired
    DiscussPostService discussPostService;

    /**
    * Description:添加评论更新数量
    * date: 2022/4/6 19:26
    * @author: MaoJY
    * @since JDK 1.8
    */
    @Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment){
        if (comment == null) {
            throw  new IllegalArgumentException("参数不能为空");
        }
        comment.setContent(sensitiveWordFilter.filter(comment.getContent()));
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        int rows=commentMapper.insertComment(comment);
        if (comment.getEntityType()== ConstantVO.ENTITY_TYPE_COMMENT){
            int count =commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return  rows;
    }

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

}