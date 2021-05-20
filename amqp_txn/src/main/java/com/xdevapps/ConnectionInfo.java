package com.xdevapps;

public class ConnectionInfo {

    private String host        = "puffin-01.rmq2.cloudamqp.com";
    private String username    = "pvwlccaf";
    private String password    = "rX34WxIxr0zi1_urGsiKIVFdGUF15BqU";
    private String virtualHost = "pvwlccaf";
    private int    port        = 5672;

    public ConnectionInfo() {
    }

    public ConnectionInfo(String host, String username, String password, String virtualHost, int port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.virtualHost = virtualHost;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    

}