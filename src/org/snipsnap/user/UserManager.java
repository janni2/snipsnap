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

import org.snipsnap.util.ConnectionManager;
import org.snipsnap.util.log.Logger;
import org.snipsnap.cache.Cache;
import org.snipsnap.jdbc.Loader;
import org.snipsnap.jdbc.FinderFactory;
import org.snipsnap.jdbc.Finder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * User manager handles all register, creation and authentication of users.
 * @author Stephan J. Schmidt
 * @version $Id$
 */
public class UserManager implements Loader {
  private static UserManager instance;

  public static synchronized UserManager getInstance() {
    if (null == instance) {
      instance = new UserManager();
    }
    return instance;
  }

  public List getAll() {
    return storageAll();
  }

  private final static String COOKIE_NAME = "SnipSnapUser";
  private final static String ATT_USER = "user";
  private final static int SECONDS_PER_YEAR = 60 * 60 * 24 * 365;

  private Map authHash = new HashMap();
  private Map robots = new HashMap();
  private List delayed;
  private Cache cache;
  private FinderFactory finders;

  protected UserManager() {
    delayed = new LinkedList();

    cache = Cache.getInstance();
    cache.setLoader(User.class, (Loader) this);

    finders = new FinderFactory("SELECT login, passwd, email, status, roles, " +
                                " cTime, mTime, lastLogin, lastAccess, lastLogout " +
                                " FROM SnipUser ", cache, (Loader) this, "login");

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
  }

  // update the auth hash by removing all entries and updating from the database
  private void updateAuthHash() {
    authHash.clear();
    Iterator users = getAll().iterator();
    while (users.hasNext()) {
      User user = (User) users.next();
      authHash.put(Password.getCookieDigest(user), user);
    }
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
          System.err.println("UserManager: invalid hash: "+auth);
        }
      }

      if (null == user) {
        String agent = request.getHeader("User-Agent");
        System.err.println("User agent of unknown user: '"+agent+"'");
        if(agent != null && agent.toLowerCase().indexOf("googlebot") != -1) {
          user = (User)robots.get("GoogleBot");
          if(null == user) {
            user = new User("GoogleBot", "GoogleBot", "http://www.googlebot.com/bot.html");
            user.setNonUser(true);
            robots.put(user.getLogin(), user);
          }
        } else {
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
    String auth = Password.getCookieDigest(user);
    // @TODO find better solution by removing by value
    updateAuthHash();

    authHash.put(auth, user);
    Cookie cookie = new Cookie(COOKIE_NAME, auth);
    cookie.setMaxAge(SECONDS_PER_YEAR);
    cookie.setPath("/");
    cookie.setComment("SnipSnap User");
    response.addCookie(cookie);
  }

  public void removeCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = getCookie(request, COOKIE_NAME);
    if (cookie != null) {
      cookie.setMaxAge(0);
      cookie.setPath("/");
      cookie.setComment("SnipSnap User");
      response.addCookie(cookie);
    }
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


  public User authenticate(String login, String passwd) {
    User user = load(login);
    if (null != user && user.getPasswd().equals(passwd)) {
      user.lastLogin();
      storageStore(user);
      return user;
    } else {
      return null;
    }
  }

  public boolean isAuthenticated(User user) {
    return user != null && !(user.isGuest() || user.isNonUser());
  }

  public Object createObject(ResultSet result) throws SQLException {
    return createUser(result);
  }

  public Object loadObject(String login) {
    return storageLoad(login);
  }

  public Class getLoaderType() {
    return User.class;
  }

  public User create(String login, String passwd, String email) {
    User user = storageCreate(login, passwd, email);
    cache.put(User.class, login, user);
    // System.err.println("createUser login="+login+" hashcode="+((Object) user).hashCode());
    return user;
  }

  public void store(User user) {
    user.setMTime(new Timestamp(new java.util.Date().getTime()));
    storageStore(user);
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
    // System.err.println("SystemStore User="+user.getLogin());
    storageStore(user);
    return;
  }

  public void remove(User user) {
    cache.remove(User.class, user.getLogin());
    storageRemove(user);
    return;
  }

  public User load(String login) {
    return (User) cache.load(User.class, login);
  }

  // Storage System dependend Methods

  private User createUser(ResultSet result) throws SQLException {
    String login = result.getString("login");
    String passwd = result.getString("passwd");
    String email = result.getString("email");
    Timestamp cTime = result.getTimestamp("cTime");
    Timestamp mTime = result.getTimestamp("mTime");
    Timestamp lastLogin = result.getTimestamp("lastLogin");
    Timestamp lastAccess = result.getTimestamp("lastAccess");
    Timestamp lastLogout = result.getTimestamp("lastLogout");
    String status = result.getString("status");
    User user = new User(login, passwd, email);
    user.setStatus(status);
    user.setRoles(new Roles(result.getString("roles")));
    user.setCTime(cTime);
    user.setMTime(mTime);
    user.setLastLogin(lastLogin);
    user.setLastAccess(lastAccess);
    user.setLastLogout(lastLogout);
    return user;
  }

  private void storageStore(User user) {
    PreparedStatement statement = null;
    Connection connection = ConnectionManager.getConnection();

    try {
      statement = connection.prepareStatement("UPDATE SnipUser SET login=?, passwd=?, email=?, status=?, roles=?, " +
                                              " cTime=?, mTime=?, lastLogin=?, lastAccess=?, lastLogout=? " +
                                              " WHERE login=?");

      statement.setString(1, user.getLogin());
      statement.setString(2, user.getPasswd());
      statement.setString(3, user.getEmail());
      statement.setString(4, user.getStatus());
      statement.setString(5, user.getRoles().toString());
      statement.setTimestamp(6, user.getCTime());
      statement.setTimestamp(7, user.getMTime());
      statement.setTimestamp(8, user.getLastLogin());
      statement.setTimestamp(9, user.getLastAccess());
      statement.setTimestamp(10, user.getLastLogout());
      statement.setString(11, user.getLogin());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      ConnectionManager.close(statement);
      ConnectionManager.close(connection);
    }
    return;
  }

  private User storageCreate(String login, String passwd, String email) {
    PreparedStatement statement = null;
    ResultSet result = null;
    Connection connection = ConnectionManager.getConnection();

    User user = new User(login, passwd, email);
    Timestamp cTime = new Timestamp(new java.util.Date().getTime());
    user.setCTime(cTime);
    user.setMTime(cTime);
    user.setLastLogin(cTime);
    user.setLastAccess(cTime);
    user.setLastLogout(cTime);

    try {
      statement = connection.prepareStatement("INSERT INTO SnipUser " +
                                              " (login, passwd, email, status, roles, " +
                                              " cTime, mTime, lastLogin, lastAccess, lastLogout) " +
                                              " VALUES (?,?,?,?,?,?,?,?,?,?)");
      statement.setString(1, login);
      statement.setString(2, passwd);
      statement.setString(3, email);
      statement.setString(4, "");
      statement.setString(5, "");
      statement.setTimestamp(6, cTime);
      statement.setTimestamp(7, cTime);
      statement.setTimestamp(8, cTime);
      statement.setTimestamp(9, cTime);
      statement.setTimestamp(10, cTime);

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      ConnectionManager.close(result);
      ConnectionManager.close(statement);
      ConnectionManager.close(connection);
    }

    return user;
  }


  private void storageRemove(User user) {
    PreparedStatement statement = null;
    Connection connection = ConnectionManager.getConnection();

    try {
      statement = connection.prepareStatement("DELETE FROM SnipUser WHERE login=?");
      statement.setString(1, user.getLogin());
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      ConnectionManager.close(statement);
      ConnectionManager.close(connection);
    }
    return;
  }

  private User storageLoad(String login) {
    System.err.println("storageLoad() User="+login);
    User user = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    Connection connection = ConnectionManager.getConnection();

    try {
      statement = connection.prepareStatement("SELECT login, passwd, email, status, roles, cTime, mTime, lastLogin, lastAccess, lastLogout " +
                                              " FROM SnipUser " +
                                              " WHERE login=?");
      statement.setString(1, login);

      result = statement.executeQuery();
      if (result.next()) {
        user = createUser(result);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      ConnectionManager.close(result);
      ConnectionManager.close(statement);
      ConnectionManager.close(connection);
    }
    return user;
  }

  private List storageAll() {
    Finder finder = finders.getFinder(" ORDER BY login");
    return finder.execute();
  }

}
