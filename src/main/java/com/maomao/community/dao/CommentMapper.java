package com.maomao.community.dao;

import com.maomao.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author MaoJY
 * @create 2022-04-06 18:51
 * @Description:
 */
@Mapper
public interface CommentMapper {
    /**
    * Description:插入一条评论
    * date: 2022/4/6 18:51
    * @author: MaoJY
    * @since JDK 1.8
    */
    int insertComment(Comment comment);

    int selectCountByEntity(int entityType,int entityId);

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    Comment  findCommentById(int id);
}