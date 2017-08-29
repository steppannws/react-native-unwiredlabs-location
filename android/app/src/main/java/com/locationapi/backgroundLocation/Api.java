package com.locationapi.backgroundLocation;

import org.json.JSONObject;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

public class Api {
  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(url, params, responseHandler);
  }

  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(url, params, responseHandler);
  }

}