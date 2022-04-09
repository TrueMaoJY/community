package com.maomao.community.dao;

import com.maomao.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author MaoJY
 * @create 2022-04-07 19:55
 * @Description:
 */
@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表，只返回最近一条消息
    List<Message> selectConversations(int userId,int offset,int limit);
    //查询当前用户的会话数量
    int selectConversationCount(int userId);
    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);
    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);
    //查询未读私信的数量
    int selectLetterUnreadCount(int userId,String conversationId);
    //插入一条消息
    int insertMessage(Message message);
    int updateStatus(List<Integer> ids,int status);

}
