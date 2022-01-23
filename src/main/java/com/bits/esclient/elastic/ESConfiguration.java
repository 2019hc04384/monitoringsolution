package com.bits.esclient.elastic;

public interface ESConfiguration {
    String getUserName();

    String getHostName();

    int getPort();

    String getProtocolScheme();

    String getPassword();

    boolean isBulkMode();
}
