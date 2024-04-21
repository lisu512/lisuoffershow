package com.example.demo.controller;

import com.example.demo.service.AccessTokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;


import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.example.demo.service.ObjectService;
import com.example.demo.entity.Confirm;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/wechat")
public class WechatMessageController {

    @Autowired
    private ObjectService objectService;

    @Autowired
    private AccessTokenService accessTokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/search")
    public ResponseEntity<List<Confirm>> searchConfirms(@RequestParam Map<String, String> params, Pageable pageable) {
        Page<Confirm> confirms = objectService.searchConfirms(params, pageable); // 使用实例调用而非静态调用
        return ResponseEntity.ok(confirms.getContent());
    }

    @PostMapping
    public void handleMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Map<String, String> parseXml = parseXml(request);
            String msgType = parseXml.get("MsgType");
            String content = parseXml.get("Content");
            String fromUserName = parseXml.get("FromUserName");
            String toUserName = parseXml.get("ToUserName");

            if (fromUserName == null || fromUserName.isEmpty()) {
                fromUserName = "黎苏";  // 如果null或空，设置默认值为"黎苏"
            }
//            Map<String, String> params = parseContent(content);
            if (Objects.equals(msgType, "text") && content.trim().equals("查询;")) {
                // 调用服务以获取分页数据
                int page = 0;
                int size = 3; // 页面大小
                var pageData = objectService.listObjectsPaginated(page, size);
                var confirms = pageData.getContent();
                var totalPages = pageData.getTotalPages();

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("数据（第1页，共").append(totalPages).append("页）:\n");
                for (Confirm confirm : confirms) {
                    messageBuilder.append(confirm.toString()).append("\n"); // 确保 Confirm 实体有适当的 toString 方法
                }

                String replyMessage = buildTextMessage(fromUserName, toUserName, messageBuilder.toString());
                out.print(replyMessage);
            } else if ("text".equals(msgType) && content.startsWith("爆料;")) {
                Map<String, String> params = parseContent(content);
                if (isValidParams(params)) {
                    Confirm confirm = createConfirmFromParams(params,fromUserName);
                    objectService.createObject(confirm);
                    String replyMessage = buildTextMessage(fromUserName, toUserName, "爆料成功！");
                    out.print(replyMessage);
                } else {
                    String replyMessage = buildTextMessage(fromUserName, toUserName, "参数不完整或有误，请按格式正确输入！");
                    out.print(replyMessage);
                }
            }
            else if ("text".equals(msgType) && content.startsWith("高级查询;")) {
                Map<String, String> params = parseContent(content);
                int page = Integer.parseInt(params.get("页码"));
                if (page > 0){page--;}
                int size = 3; // 页面大小
                Pageable pageable = PageRequest.of(page, size); // 查询前3条记录
                Page<Confirm> result = objectService.searchConfirms(params, pageable);
                StringBuilder replyBuilder = new StringBuilder();
                replyBuilder.append("高级查询结果（每页显示3条，共 ").append(result.getTotalPages()).append(" 页）:\n");
                for (Confirm confirm : result.getContent()) {
                    replyBuilder.append(confirm.toString()).append("\n");
                }
                String replyMessage = buildTextMessage(fromUserName, toUserName, replyBuilder.toString());
                out.print(replyMessage);
            }
            else if ("text".equals(msgType) && content.startsWith("批量上传;")) {
                List<Confirm> confirms = parseBulkContent(content,fromUserName);
                if (confirms.isEmpty()) {
                    String replyMessage = buildTextMessage(fromUserName, toUserName, "参数不完整或有误，请检查后再试！");
                    out.print(replyMessage);
                } else {
                    List<Confirm> savedConfirms = objectService.createObjects(confirms);

                    String replyMessage = buildTextMessage(fromUserName, toUserName, "成功爆料 " + savedConfirms.size() + " 条记录！");
                    out.print(replyMessage);
                }
            }
            else if ("text".equals(msgType) && content.startsWith("删除;")) {
                String[] parts = content.split(";");
                if (parts.length >= 2) {
                    // 尝试从“编号:6”这样的格式中提取出编号
                    String[] idPart = parts[1].split(":");  // 分割“编号:6”
                    if (idPart.length == 2 && idPart[0].trim().equals("编号")) {
                        try {
                            int id = Integer.parseInt(idPart[1].trim());  // 提取编号部分并转换为整数
                            boolean isDeleted = objectService.deleteObject(id, fromUserName);
                            String replyMessage;
                            if (isDeleted) {
                                replyMessage = buildTextMessage(fromUserName, toUserName, "删除成功！");
                            } else {
                                replyMessage = buildTextMessage(fromUserName, toUserName, "删除失败：编号不存在或无权操作该记录。");
                            }
                            out.print(replyMessage);
                        } catch (NumberFormatException e) {
                            String replyMessage = buildTextMessage(fromUserName, toUserName, "删除失败：请提供有效的编号。");
                            out.print(replyMessage);
                        }
                    } else {
                        String replyMessage = buildTextMessage(fromUserName, toUserName, "删除失败：请正确提供编号。");
                        out.print(replyMessage);
                    }
                } else {
                    String replyMessage = buildTextMessage(fromUserName, toUserName, "删除失败：请提供编号。");
                    out.print(replyMessage);
                }
            }
            else if ("text".equals(msgType) && content.startsWith("替换;")) {
                Map<String, String> params = parseContent(content);
                if (isValidParams(params)) {
                    try {
                        int id = Integer.parseInt(params.get("编号").trim());  // 提取并解析编号
                        Confirm newConfirm = createConfirmFromParams(params, fromUserName);
                        Optional<Confirm> updatedConfirm = objectService.replaceObject(id, newConfirm, fromUserName);
                        String replyMessage;
                        if (updatedConfirm.isPresent()) {
                            replyMessage = buildTextMessage(fromUserName, toUserName, "替换成功！");
                        } else {
                            replyMessage = buildTextMessage(fromUserName, toUserName, "替换失败：编号不存在或无权操作该记录。");
                        }
                        out.print(replyMessage);
                    } catch (NumberFormatException e) {
                        String replyMessage = buildTextMessage(fromUserName, toUserName, "替换失败：请提供有效的编号。");
                        out.print(replyMessage);
                    }
                } else {
                    String replyMessage = buildTextMessage(fromUserName, toUserName, "参数不完整或有误，请按格式正确输入！");
                    out.print(replyMessage);
                }
            }
            else if ("text".equals(msgType) && content.startsWith("更新;")) {
                Map<String, String> params = parseContent(content);
                try {
                    int id = Integer.parseInt(params.get("编号").trim());  // 提取并解析编号
                    Optional<Optional<?>> updatedConfirm = objectService.updateObject(id, params, fromUserName);
                    String replyMessage;
                    if (updatedConfirm.isPresent()) {
                        replyMessage = buildTextMessage(fromUserName, toUserName, "更新成功");
                    } else {
                        replyMessage = buildTextMessage(fromUserName, toUserName, "更新失败：编号不存在或无权操作该记录。");
                    }
                    out.print(replyMessage);
                } catch (NumberFormatException e) {
                    String replyMessage = buildTextMessage(fromUserName, toUserName, "更新失败：请提供有效的编号。");
                    out.print(replyMessage);
                }
            }

            else {
                String replyMessage = buildTextMessage(fromUserName, toUserName, "黎苏欢迎你：参考相关文档：https://workdrive.zoho.com.cn/file/7eiy8b88f581ea1f24ed99adfa7ec13f202bb");
                out.print(replyMessage);
            }
        } finally {
            out.close();
        }
    }

    private static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<>();
        InputStream inputStream = request.getInputStream();
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();
        for (Element e : elementList) {
            map.put(e.getName(), e.getText());
        }
        inputStream.close();
        return map;
    }
    private Map<String, String> parseContent(String content) {
        Map<String, String> params = new HashMap<>();
        String[] parts = content.split(";");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                params.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        // 如果未提供页码参数，设置默认值
        params.putIfAbsent("页码", "0");  // 默认页码为第0页
        return params;
    }


    private boolean isValidParams(Map<String, String> params) {
        // Check required fields and salary is a positive integer
        int salary = Integer.parseInt(params.get("薪资").trim());
        return params.containsKey("公司") && params.containsKey("岗位") &&
                params.containsKey("城市") && params.containsKey("薪资") &&
                params.containsKey("学历") && params.containsKey("行业") &&
                params.containsKey("类型") && !params.get("薪资").isEmpty() &&
                salary > 0;
    }



    private String buildTextMessage(String fromUserName, String toUserName, String content) {
        return "<xml>" +
                "<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + toUserName + "]]></FromUserName>" +
                "<CreateTime>" + System.currentTimeMillis() / 1000 + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + content + "]]></Content>" +
                "</xml>";
    }

    private String formatSearchResults(List<Confirm> results) {
        StringBuilder builder = new StringBuilder();
        for (Confirm confirm : results) {
            builder.append(confirm.toString()).append("\n");
        }
        return builder.toString();
    }
    private List<Confirm> parseBulkContent(String content,String unionId) {
        List<Confirm> confirms = new ArrayList<>();
        String[] entries = content.split("\\*");
        for (String entry : entries) {
            Map<String, String> params = parseContent(entry);
            if (isValidBulkParams(params)) {
                Confirm confirm = createConfirmFromParams(params,unionId);
                confirms.add(confirm);
            }
        }
        return confirms;
    }

    private boolean isValidBulkParams(Map<String, String> params) {
        try {
            // 确保薪资是一个正数
            int salary = Integer.parseInt(params.get("薪资").trim());
        } catch (NumberFormatException e) {
            return false; // 如果薪资不是有效的小数，则返回 false
        }
        return params.containsKey("公司") && !params.get("公司").isEmpty() &&
                params.containsKey("岗位") && !params.get("岗位").isEmpty() &&
                params.containsKey("城市") && !params.get("城市").isEmpty() &&
                params.containsKey("薪资") && !params.get("薪资").isEmpty() &&
                params.containsKey("学历") && !params.get("学历").isEmpty() &&
                params.containsKey("行业") && !params.get("行业").isEmpty() &&
                params.containsKey("类型") && (params.get("类型").equals("校招") || params.get("类型").equals("社招"));
    }


    private Confirm createConfirmFromParams(Map<String, String> params,String unionID) {
        Confirm confirm = new Confirm();
        confirm.setCompany(params.get("公司"));
        confirm.setPosition(params.get("岗位"));
        confirm.setCity(params.get("城市"));
        confirm.setSalary(Integer.parseInt(params.get("薪资").trim()));
        confirm.setEducation(params.get("学历"));
        confirm.setIndustry(params.get("行业"));
        confirm.setType(params.get("类型"));
        confirm.setRemarks(params.getOrDefault("备注", ""));
        confirm.setCreateUser(unionID); // 设置为当前微信用户
        return confirm;
    }



}
