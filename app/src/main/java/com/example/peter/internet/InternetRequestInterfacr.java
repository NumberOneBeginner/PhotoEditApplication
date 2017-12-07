package com.example.peter.internet;

import java.util.Map;

/**
 * Created by peter on 2017/11/21.
 */

public interface InternetRequestInterfacr {

    void getRequest(String url);

    void getRequest(String url, Map<Object ,Object> paramsMap);

    void postRequest(String url, Map<Object ,Object> paramsMap);

    ;


}
