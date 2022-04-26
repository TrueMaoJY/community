package com.maomao.community.service;

import com.maomao.community.dao.MessageMapper;
import com.maomao.community.entity.Message;
import com.maomao.community.filter.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author MaoJY
 * @create 2022-04-07 20:01
 * @Description:
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;
    /**
     * Description:查询当前用户的会话列表，只返回最近一条消息
     * date: 2022/4/7 20:03
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    public List<Message> selectConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }
    /**
     * Description:查询当前用户的会话数量
     * date: 2022/4/7 20:14
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    public int selectConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }
    /**
     * Description:查询某个会话所包含的私信列表
     * date: 2022/4/7 20:14
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    public List<Message> selectLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }
    /**
     * Description:查询某个会话所包含的私信数量
     * date: 2022/4/7 20:15
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    public int selectLetterCount(String conversationId) {
         return messageMapper.selectLetterCount(conversationId);
    }
    /**
    * Description:查询未读私信的数量
    * date: 2022/4/7 20:16
    * @author: MaoJY
    * @since JDK 1.8
    */
    public  int selectLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }
    /**
    * Description:插入一条私信
    * date: 2022/4/9 20:51
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int insertMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveWordFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }
    /**
    * Description:更新消息为已读的状态
    * date: 2022/4/9 20:53
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int readMessage(List<Integer> ids,int status){
        return messageMapper.updateStatus(ids,status);
    }

    /**
    * Description:查询最新的一条某一个主题的通知
    * date: 2022/4/22 16:23
    * @author: MaoJY
    * @since JDK 1.8
    */
    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }
    /**
    * Description:查询某一主题的所有记录
    * date: 2022/4/22 16:25
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }
    /**
    * Description:查询某一主题或者所有主题未读的记录数
    * date: 2022/4/22 16:26
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int findNoticeUnread(int userId,String topic){
        return messageMapper.selectNoticeUnread(userId,topic);
    }
    /**
    * Description:查询所有主题的系统通知
    * date: 2022/4/25 18:22
    * @author: MaoJY
    * @since JDK 1.8
    */
    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}