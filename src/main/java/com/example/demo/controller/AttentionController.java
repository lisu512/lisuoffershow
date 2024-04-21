package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wechat") // 统一的根路径
public class AttentionController {

    private static final Logger logger = LoggerFactory.getLogger(AttentionController.class);

    @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleEvent(@RequestBody String requestBody) {
        try {
            // 检查是否是订阅事件
            if (requestBody.contains("<Event><![CDATA[subscribe]]></Event>")) {
                // 构建回复消息
                String responseMessage = buildResponseMessage(requestBody);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(responseMessage);
            }
            // 如果不是关注事件，返回空字符串
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body("");
        } catch (Exception e) {
            logger.error("Error handling wechat event", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String buildResponseMessage(String requestBody) {
        // 从请求中提取FromUserName和ToUserName
        String fromUserName = extractTagValue(requestBody, "FromUserName");
        String toUserName = extractTagValue(requestBody, "ToUserName");

        // 构建XML格式的响应消息
        return "<xml>" +
                "<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + toUserName + "]]></FromUserName>" +
                "<CreateTime>" + System.currentTimeMillis() / 1000 + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[黎苏欢迎您]]></Content>" +
                "</xml>";
    }

    // 简单解析XML标签内容的方法
    private String extractTagValue(String xml, String tagName) {
        String openTag = "<" + tagName + "><![CDATA[";
        String closeTag = "]]></" + tagName + ">";
        if (xml.contains(openTag) && xml.contains(closeTag)) {
            int start = xml.indexOf(openTag) + openTag.length();
            int end = xml.indexOf(closeTag);
            return xml.substring(start, end);
        }
        return "";
    }
}
