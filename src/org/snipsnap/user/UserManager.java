/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */
package org.snipsnap.user;

import org.radeox.util.logging.Logger;
import org.snipsnap.app.Application;
import org.snipsnap.snip.storage.UserStorage;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

import picocontainer.Container;
import nanocontainer.StringRegistrationNanoContainer;
import nanocontainer.StringRegistrationNanoContainerImpl;

/**
 * User manager handles all register, creation and authentication of users.
 * @author Stephan J. Schmidt
 * @version $Id$
 */
public class UserManager {
  private static Container container;

  public static synchronized UserManager getInstance() {
    if (null == container) {
      StringRegistrationNanoContainer c  =
          new StringRegistrationNanoContainerImpl.Default();

      try {
        c.registerComponent("org.snipsnap.snip.storage.JDBCUserStorage");
        c.registerComponent("org.snipsnap.user.UserManager");
        c.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
      container = c;
    }

    return (UserManager) container.getComponent(UserManager.class);
  }

  public static synchronized void removeInstance() {
    // @TODO: This is a workaround
    // there should be no need to remove an Instance
    // perhaps reinitialice or start/stop would be better
    container = null;
  }

  private final static String COOKIE_NAME = "SnipSnapUser";
  private final static String ATT_USER = "user";
  private final static int SECONDS_PER_YEAR = 60 * 60 * 24 * 365;

  private Map authHash = new HashMap();
  private Map robots = new HashMap();
  private Map robotIds = new HashMap();
  private List delayed;
  private Map authKeys;

  private UserStorage storage;

  public UserManager(UserStorage storage) {
    this.storage = storage;

    delayed = new LinkedList();
    authKeys = new HashMap();

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      public void run() {
        synchronized (delayed) {
          ListIterator iterator = delayed.listIterator();
          while (iterator.hasNext()) {
            User user = (User) iterator.next();
            systemStore(user);
            iterator.remove();
          }
        }
      }
      // execute after 5 minutes and then
      // every 5 minutes
    }, 5 * 60 * 1000, 5 * 60 * 1000);

    try {
      BufferedReader crawler = new BufferedReader(
          new InputStreamReader(new FileInputStream("conf/robotdetect.txt")));
      String line = null;
      int ln = 0;
      while ((line = crawler.readLine()) != null) {
        ln++;
        if (line.length() > 0 && !line.startsWith("#")) {
          try {
            String id = line.substring(0, line.indexOf(' '));
            String url = line.substring(line.indexOf(' ') + 1);
            if (url.indexOf("IGNORE") != -1) {
              robotIds.put(id, "IGNORE");
            } else {
              robotIds.put(id, url);
            }
          } catch (Exception e) {
            Logger.warn("UserManager: conf/robotdetect.txt line " + ln + ": syntax error", e);
          }
        }
      }
    } catch (IOException e) {
      Logger.warn("UserManager: unable to read conf/robotdetect.txt", e);
    }
  }

  public List getAll() {
    return storage.storageAll();
  }

  // update the auth hash by removing all entries and updating from the database
  private void updateAuthHash() {
    authHash.clear();
    Iterator users = getAll().iterator();
    while (users.hasNext()) {
      User user = (User) users.next();
      authHash.put(Digest.getCookieDigest(user), user);
    }
  }

  public int getUserCount() {
    return storage.storageUserCount();
  }

  /**
   * Get user from session or cookie.
   */
  public User getUser(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(true);
    User user = (User) session.getAttribute(ATT_USER);
    if (null == user) {
      Cookie cookie = getCookie(request, COOKIE_NAME);
      if (cookie != null) {
        String auth = cookie.getValue();
        if (!authHash.containsKey(auth)) {
          updateAuthHash();
        }

        user = (User) authHash.get(auth);
        if (user != null) {
          user = authenticate(user.getLogin(), user.getPasswd());
          setCookie(request, response, user);
        } else {
          Logger.warn("UserManager: invalid hash: " + auth);
        }
      }

      if (null == user) {
        String agent = request.getHeader("User-Agent");
        Iterator it = robotIds.keySet().iterator();
        while (agent != null && user == null && it.hasNext()) {
          String key = (String) it.next();
          if (agent.toLowerCase().indexOf(key.toLowerCase()) != -1) {
            user = (User) robots.get(key);
            if (null == user) {
              user = new User(key, key, (String) robotIds.get(key));
              user.setNonUser(true);
              robots.put(key, user);
            }
            break;
          }
        }

        if (user != null) {
          Logger.debug("Found robot: " + user);
        } else {
          Logger.debug("User agent of unknown user: '" + agent + "'");
          user = new User("Guest", "Guest", "");
          user.setGuest(true);
        }
        removeCookie(request, response);
      }
      session.setAttribute(ATT_USER, user);
    }
    return user;
  }


  /**
   * Set cookie with has of encoded user/pass and last login time.
   */
  public void setCookie(HttpServletRequest request, HttpServletResponse response, User user) {
    String auth = Digest.getCookieDigest(user);
    // @TODO find better solution by removing by value
    updateAuthHash();

    authHash.put(auth, user);
    Cookie cookie = new Cookie(COOKIE_NAME, auth);
    cookie.setMaxAge(SECONDS_PER_YEAR);
    cookie.setPath(getCookiePath());
    cookie.setComment("SnipSnap User");
    response.addCookie(cookie);
  }


  public void removeCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = getCookie(request, COOKIE_NAME);
    if (cookie != null) {
      cookie.setPath(getCookiePath());
      cookie.setMaxAge(0);
      response.addCookie(cookie);
    }
  }

  private String getCookiePath() {
    String path;
    try {
      path = new URL(Application.get().getConfiguration().getUrl()).getPath();
      if (path == null || path.length() == 0) {
        path = "/";
      }
    } catch (MalformedURLException e) {
      Logger.warn("Malformed URL: "+ Application.get().getConfiguration().getUrl(), e);
      path = "/";
    }
    return path;
  }


  /**
   * Helper method for getUser to extract user from request/cookie/session
   * @param request
   * @param name
   * @return
   */
  public Cookie getCookie(HttpServletRequest request, String name) {
    Cookie cookies[] = request.getCookies();
    for (int i = 0; cookies != null && i < cookies.length; i++) {
      if (cookies[i].getName().equals(name)) {
        return cookies[i];
      }
    }
    return null;
  }

  // Handles forgotten passwords

  public String getPassWordKey() {
    return getPassWordKey(Application.get().getUser());
  }

  public String getPassWordKey(User user) {
    String key = Digest.getDigest(Integer.toString((new Random()).nextInt()));
    authKeys.put(key, user);
    return key;
  }

  public User changePassWord(String key, String passwd) {
    User user = (User) authKeys.get(key);
    if (null != user) {
      user.setPasswd(passwd);
      storage.storageStore(user);
      authKeys.remove(key);
    }
    return user;
  }

  public User getUserFromKey(String key) {
    return (User) authKeys.get(key);
  }

  public User authenticate(String login, String passwd) {
    User user = load(login);
/*
    System.out.println("user: "+user);
    System.out.println("check: unencrypted: "+user.getPasswd().equals(passwd));
    System.out.println(passwd+"-"+Digest.getDigest(passwd)+"-"+user.getPasswd());
    System.out.println("check: encrypted: "+Digest.authenticate(passwd, user.getPasswd()));
*/
    //@TODO split authenticate and lastLogin
    if (null != user &&
        (user.getPasswd().equals(passwd) ||
        Digest.authenticate(passwd, user.getPasswd()))) {
      user.lastLogin();
      storage.storageStore(user);
      return user;
    } else {
      return null;
    }
  }

  public boolean isAuthenticated(User user) {
    return user != null && !(user.isGuest() || user.isNonUser());
  }

  public Object loadObject(String login) {
    return storage.storageLoad(login);
  }

  public Class getLoaderType() {
    return User.class;
  }

  public User create(String login, String passwd, String email) {
    passwd = Digest.getDigest(passwd);
    User user = storage.storageCreate(login, passwd, email);
    return user;
  }

  public void store(User user) {
    user.setMTime(new Timestamp(new java.util.Date().getTime()));
    storage.storageStore(user);
    return;
  }

  public void delayedStore(User user) {
    synchronized (delayed) {
      if (!delayed.contains(user)) {
        delayed.add(user);
      }
    }
  }

  public void systemStore(User user) {
    // Logger.debug("SystemStore User="+user.getLogin());
    storage.storageStore(user);
    return;
  }

  public void remove(User user) {
    storage.storageRemove(user);
    return;
  }

  public User load(String login) {
    return storage.storageLoad(login);
  }
}
