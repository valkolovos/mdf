package org.mdf.mockdata.remote;

public interface HttpClientResponse {
    public int getStatus();
    public byte[] getResponse();
}
