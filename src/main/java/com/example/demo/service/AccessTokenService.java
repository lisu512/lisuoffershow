    package com.example.demo.service;

    import lombok.Getter;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;

    import javax.annotation.PostConstruct;
    import java.util.concurrent.Executors;
    import java.util.concurrent.ScheduledExecutorService;
    import java.util.concurrent.TimeUnit;

    @Service
    public class AccessTokenService {
        private static final Logger logger = LoggerFactory.getLogger(AccessTokenService.class);

        private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";

        @Value("${wx.appid}")
        private String appId;

        @Value("${wx.secret}")
        private String appSecret;

        @Getter
        private String accessToken;

        private final RestTemplate restTemplate = new RestTemplate();

        @PostConstruct
        public void init() {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(this::refreshToken, 0, 7100, TimeUnit.SECONDS);
        }

        private void refreshToken() {
            try {
                String url = TOKEN_URL.replace("{appid}", appId).replace("{secret}", appSecret);
                AccessTokenResponse response = restTemplate.getForObject(url, AccessTokenResponse.class);
                if (response != null && response.getAccessToken() != null) {
                    accessToken = response.getAccessToken();
                    logger.info("Refreshed Access Token: {}", accessToken);  // 使用日志记录而不是System.out.println
                }
            } catch (Exception e) {
                logger.error("Failed to refresh access token", e);  // 记录错误日志
            }
        }

    }

