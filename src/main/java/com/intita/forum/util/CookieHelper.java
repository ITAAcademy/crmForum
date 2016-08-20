package com.intita.forum.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieHelper {
	/**
	  * Adds a cookie to the web browser
	  * */
	  public static void saveCookie(String cookieName, String value, int maxAge, HttpServletResponse response) {
	    Cookie cookie = new Cookie(cookieName, value);
	    cookie.setMaxAge(maxAge);
	    response.addCookie(cookie);
	  }
	  public static String getCookieValue(String cookieName, HttpServletRequest request) {
		    String value = null;
		    Cookie[] cookies = request.getCookies();
		    if (cookies != null) {
		      int i = 0;
		      boolean cookieExists = false;
		      while (!cookieExists && i < cookies.length) {
		        if (cookies[i].getName().equals(cookieName)) {
		          cookieExists = true;
		          value = cookies[i].getValue();
		        } else {
		          i++;
		        }
		      }
		    }
		    return value;
		  }
}
