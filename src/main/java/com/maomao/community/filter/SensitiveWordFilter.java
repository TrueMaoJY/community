package com.maomao.community.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;

import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author MaoJY
 * @create 2022-04-02 22:08
 * @Description:
 */
@Data
@Slf4j
@Getter
@Component
public class SensitiveWordFilter {
    private  TrieTree root;
    private static final  String PLACEMENT="***";
    public SensitiveWordFilter(){
        root=new TrieTree();
        try ( InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
              BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
            String key;
            while((key= reader.readLine())!=null){
                addKeyWord(key);
            }

        } catch (IOException e) {
            log.info("读取不到敏感词文件"+e.getMessage());
        }
    }
    /**
    * Description:在调用构造器之后调用该方法，读取配置文件中的信息
    * date: 2022/4/3 21:25
    * @author: MaoJY
    * @since JDK 1.8
    */
//    @PostConstruct
//    public void init(){
//        不生效
//      try ( InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
//    BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
//        String key;
//        while((key= reader.readLine())!=null){
//            addKeyWord(key);
//        }
//
//    } catch (IOException e) {
//        log.info("读取不到敏感词文件"+e.getMessage());
//    }
//    }
    /**
    * Description:返回过滤以后的单词
    * date: 2022/4/3 21:41
    * @author: MaoJY
    * @since JDK 1.8
    */
    public String filter(String text){
        if (text == null) {
            return null;
        }
        TrieTree temp=root;
        int begin=0,position=0;
        StringBuilder sb =new StringBuilder();
        while(begin<text.length()){
            char c=text.charAt(position);
            if(isSymbol(c)){
                if(temp==root){
                    sb.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }
            temp=temp.getTrieTree(c);
            if (temp == null) {
                sb.append(text.charAt(begin));
               position=++begin;
               temp=root;
            }else if(temp.isTail){
                sb.append(PLACEMENT);
                begin=++position;
            }else {
                position++;
            }
            if(position==text.length()&&begin<text.length()){
                sb.append(text.charAt(begin));
                position=++begin;
            }
        }
//        sb.append(text.substring(begin));
//        return sb.toString();
        return sb.toString();
    }
    /**
    * Description:是否非字符
    * date: 2022/4/3 21:53
    * @author: MaoJY
    * @since JDK 1.8
    */
    private boolean isSymbol(char c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c< 0x2E80 || c> 0x9FFF);
    }
    /**
    * Description:构造前缀树
    * date: 2022/4/3 21:41
    * @author: MaoJY
    * @since JDK 1.8
    */
    private void addKeyWord(String key){
        TrieTree temp=root;
        for (int i = 0; i < key.length(); i++) {
            char c=key.charAt(i);
            if(!temp.trieTreeMap.containsKey(c)){
                TrieTree node=new TrieTree();
               temp.addChar(c,node);
            }
            TrieTree trieTree = temp.getTrieTree(c);
            temp=trieTree;
        }
        temp.isTail=true;
    }

    @Data
    @AllArgsConstructor
   private class TrieTree{
       private boolean isTail;
       private Map<Character,TrieTree> trieTreeMap;
       public TrieTree(){
           trieTreeMap=new HashMap<>();
           isTail=false;
       }
       public TrieTree getTrieTree(char c){
           if (trieTreeMap.containsKey(c)) {
               return trieTreeMap.get(c);
           }
           return null;
       }
       public void addChar(char c,TrieTree trieTree){
           trieTreeMap.put(c,trieTree);
       }
   }
}