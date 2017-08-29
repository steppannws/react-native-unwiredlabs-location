package com.locationapi.backgroundLocation;

import org.json.JSONObject;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

public class Api {
  private static AsyncHttpClient client = new AsyncHttpClient();

  /**
   * Async GET
   * 
   * @param {String} url
   * @param {RequestParams} params
   * @param {AsyncHttpResponseHandler} responseHandler
   */
  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(url, params, responseHandler);
  }

  /**
   * Async POST
   * 
   * @param {String} url
   * @param {RequestParams} params
   * @param {AsyncHttpResponseHandler} responseHandler
   */
  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(url, params, responseHandler);
  }

}