package org.example.ws;

/**
 * @author wangmin
 * @version 1.0
 * @description SSL配置类，用于配置WebSocket连接的SSL上下文
 * @date 2025/9/21 19:35
 */

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
public class SSLConfig {

    private static SSLContext sharedSSLContext;

    /**
     * 获取或创建共享的SSL上下文
     * @return SSLContext实例
     */
    public static SSLContext getOrCreateSSLContext() {
        // 设置系统代理用于抓包（确保本地有代理服务器运行在8888端口）
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "8888");
        if (sharedSSLContext == null) {
            try {
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };

                // Install the all-trusting trust manager
                sharedSSLContext = SSLContext.getInstance("TLSv1.3");
                sharedSSLContext.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (Exception e) {
                throw new RuntimeException("SSL 上下文创建失败", e);
            }
        }
        return sharedSSLContext;
    }

    @PostConstruct
    public void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLSv1.3");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            throw new RuntimeException("SSL 配置失败", e);
        }
    }
}