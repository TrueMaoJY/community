package com.maomao.community.controller;

import com.maomao.community.entity.Message;
import com.maomao.community.entity.Page;
import com.maomao.community.entity.User;
import com.maomao.community.service.MessageService;
import com.maomao.community.service.UserService;
import com.maomao.community.util.HostHolder;
import com.maomao.community.util.JsonUtil;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.RespBean;
import com.maomao.community.vo.RespBeanEnum;
import javafx.beans.binding.ObjectExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author MaoJY
 * @create 2022-04-07 20:23
 * @Description:
 */
@Controller
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @RequestMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setPath("/message/letter/list");
        page.setLimit(5);
        page.setRows(messageService.selectConversationCount(user.getId()));
        List<Message> conversationList =
                messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        int letterUnreadCount = 0;
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
                int count = messageService.selectLetterUnreadCount(user.getId(), message.getConversationId());
                map.put("unreadCount", count);
                letterUnreadCount += count;
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }

        int noticeUnreadCount = messageService.findNoticeUnread(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        model.addAttribute("conversations", conversations);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    /**
     * Description:私信详情
     * date: 2022/4/7 21:50
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    @RequestMapping("/letter/detail/{conversationId}")
    public String letterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        User user = hostHolder.getUser();
        page.setPath("/message/letter/detail/" + conversationId);
        page.setLimit(5);
        page.setRows(messageService.selectLetterCount(conversationId));
        List<Message> letterList = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids, 1);
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", getLetterTarget(conversationId));
        return "/site/letter-detail";
    }

    /**
     * Description:获取接收方用户信息
     * date: 2022/4/9 21:38
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    private User getLetterTarget(String conversationId) {
        String[] s = conversationId.split("_");
        int id1 = Integer.parseInt(s[0]);
        int id2 = Integer.parseInt(s[1]);
        if (id1 == hostHolder.getUser().getId()) {
            return userService.findUserById(id2);
        }
        return userService.findUserById(id1);
    }

    /**
     * Description:获取未读消息id
     * date: 2022/4/9 21:39
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    private List<Integer> getLetterIds(List<Message> list) {
        List<Integer> ids = new ArrayList<>();
        if (!list.isEmpty()) {
            for (Message message : list) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * Description:处理异步发送私信请求
     * date: 2022/4/9 20:55
     *
     * @author: MaoJY
     * @since JDK 1.8
     */
    @RequestMapping("/letter/send")
    @ResponseBody
    public Object sendLetter(String toName, String content) {

        User toUser = userService.findUserByName(toName);
        if (toUser == null) {
            return RespBean.error(RespBeanEnum.TOUSER_NOT_EXIST);
        }
        User fromUser = hostHolder.getUser();
        String conversationId = "";
        if (fromUser.getId() > toUser.getId()) {
            conversationId = toUser.getId() + "_" + fromUser.getId();
        } else {
            conversationId = fromUser.getId() + "_" + toUser.getId();
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setConversationId(conversationId);
        message.setFromId(fromUser.getId());
        message.setToId(toUser.getId());
        messageService.insertMessage(message);
        return RespBean.success();
    }
    //TODO 如果有一个类别没有就会报错，耦合度太高
    @RequestMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        int noticeUnreadCount = 0;
        //查询评论类通知
        Message lastMessage = messageService.findLatestNotice(user.getId(), ConstantVO.TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if (lastMessage != null) {
            messageVo.put("message", lastMessage);
            String content = HtmlUtils.htmlUnescape(lastMessage.getContent());
            Map<String, Object> data = JsonUtil.jsonStr2Object(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            //全部数量
            int noticeCount = messageService.findNoticeCount(user.getId(), ConstantVO.TOPIC_COMMENT);
            //未读数量
            int noticeUnread = messageService.findNoticeUnread(user.getId(), ConstantVO.TOPIC_COMMENT);
            noticeUnreadCount += noticeUnread;
            messageVo.put("noticeCount", noticeCount);
            messageVo.put("noticeUnread", noticeUnread);
            model.addAttribute("commentNotice", messageVo);
        }


        //查询点赞类通知
        lastMessage = messageService.findLatestNotice(user.getId(), ConstantVO.TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (lastMessage != null) {
            messageVo.put("message", lastMessage);
            String content = HtmlUtils.htmlUnescape(lastMessage.getContent());
            Map<String, Object> data = JsonUtil.jsonStr2Object(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            //全部数量
            int noticeCount = messageService.findNoticeCount(user.getId(), ConstantVO.TOPIC_LIKE);
            //未读数量
            int noticeUnread = messageService.findNoticeUnread(user.getId(), ConstantVO.TOPIC_LIKE);
            noticeUnreadCount += noticeUnread;
            messageVo.put("noticeCount", noticeCount);
            messageVo.put("noticeUnread", noticeUnread);
            model.addAttribute("likeNotice", messageVo);
        }


        //查询关注类通知
        lastMessage = messageService.findLatestNotice(user.getId(), ConstantVO.TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (lastMessage != null) {
            messageVo.put("message", lastMessage);
            String content = HtmlUtils.htmlUnescape(lastMessage.getContent());
            Map<String, Object> data = JsonUtil.jsonStr2Object(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            //全部数量
            int noticeCount = messageService.findNoticeCount(user.getId(), ConstantVO.TOPIC_FOLLOW);
            //未读数量
            int noticeUnread = messageService.findNoticeUnread(user.getId(), ConstantVO.TOPIC_FOLLOW);
            noticeUnreadCount += noticeUnread;
            messageVo.put("noticeCount", noticeCount);
            messageVo.put("noticeUnread", noticeUnread);
            model.addAttribute("followNotice", messageVo);
        }


        //查询未读私信数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "site/notice";
    }

    @RequestMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();
        page.setPath("/message/notice/detail/"+topic);
        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String ,Object>> noticeVOList=new ArrayList<>();
        for (Message notice : notices) {
            Map<String,Object> message=new HashMap<>();
            message.put("notice",notice);
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            HashMap<String,Object> data = JsonUtil.jsonStr2Object(content, HashMap.class);
            message.put("user",userService.findUserById((Integer)data.get("userId")));
            message.put("entityType",data.get("entityType"));
            message.put("entityId",data.get("entityId"));
            message.put("postId",data.get("postId"));
            //通知作者
            message.put("fromUser",userService.findUserById(notice.getFromId()));
            noticeVOList.add(message);
        }
        model.addAttribute("notices",noticeVOList);
        //设置已读
        List<Integer> ids = getLetterIds(notices);
        if(!ids.isEmpty()){
            messageService.readMessage(ids,1);
        }
        return "site/notice-detail";
    }
}