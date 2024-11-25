package com.attempt.npoc.entity;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import com.attempt.npoc.http.HttpURLConnectionResponse;
import com.attempt.npoc.http.RawHttp;
import com.attempt.npoc.utils.ProxyConfig;
import lombok.Data;

import javax.net.ssl.*;


@Data
public class ExecuteRequest {
    private RawHttp rawHttp;
    // 忽略 SSL 证书验证的方法
    public static void ignoreSslVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // 禁用主机名验证
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    public HttpURLConnectionResponse sendHttpRequest(String path, String host, String method, Map<String, String> headers, String body) throws Exception {
        String uri = host + path;
        // 忽略 SSL 验证
        ignoreSslVerification();
        URL url = new URL(uri);
        HttpURLConnection connection;
        // 创建代理服务器
        if(ProxyConfig.IS_PROXY.equals("true")){
            InetSocketAddress addr = new InetSocketAddress(ProxyConfig.IP_ADDR,ProxyConfig.PORT_ADDR);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr); // http 代理
             connection = (HttpURLConnection) url.openConnection(proxy);
        }else {
             connection = (HttpURLConnection) url.openConnection();
        }
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        
        try {
            // 根据不同的 HTTP 方法创建请求
            if (method.equalsIgnoreCase("GET")) {
                connection.setRequestMethod("GET");
            } else if (method.equalsIgnoreCase("POST")) {
                connection.setRequestMethod("POST");
                // 允许向服务器发送数据
                connection.setDoOutput(true);
                if (body != null) {
                    // 发送请求体
                    OutputStream outputStream = connection.getOutputStream();
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                    outputStream.close();
                }
            } else if (method.equalsIgnoreCase("PUT")) {
                connection.setRequestMethod("PUT");
                if (body != null) {
                    // 发送请求体
                    OutputStream outputStream = connection.getOutputStream();
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                    outputStream.close();
                }
            } else if (method.equalsIgnoreCase("DELETE")) {
                connection.setRequestMethod("DELETE");
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
            // 处理特殊的 DNSLog 请求逻辑
            Thread.sleep(200);
            if (url.toString().equalsIgnoreCase("http://www.dnslog.cn/getrecords.php")) {
                for (int i = 0; i < 4; i++) {
                    String responseBody = connection.getResponseMessage();
                    if (responseBody.contains("dnslog.cn")) {
                        break;
                    }
                }
            }
            HttpURLConnectionResponse httpURLConnectionResponse = new HttpURLConnectionResponse(connection);
            connection.disconnect();
            return httpURLConnectionResponse;
        } catch (Exception e) {
            connection.disconnect();
            throw new IllegalArgumentException("url:"+url+"发生错误:"+e);
        } finally {
            connection.disconnect();
        }
    }
}
