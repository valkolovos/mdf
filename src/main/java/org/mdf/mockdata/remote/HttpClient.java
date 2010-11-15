package org.mdf.mockdata.remote;

import java.net.URL;
import java.util.Map;

public interface HttpClient {
    
    public HttpClientResponse doRequest(URL requestURL, Map<String, String[]> requestParameters, byte[] body) throws Exception;

}
