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
package org.snipsnap.net.filter;

import org.snipsnap.app.Application;
import org.snipsnap.config.AppConfiguration;
import org.radeox.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * A ServletFilter that parses multipart/form-data requests and wraps the data into
 * a HttpRequestWrapper. It uses JavaMail API for parsing MIME bodies.
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class MultipartFilter implements Filter {

  private FilterConfig config = null;

  public void init(FilterConfig config) throws ServletException {
    this.config = config;
  }

  public void destroy() {
    config = null;
  }

  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;


    // if in doubt use application character encoding
    if (null == req.getCharacterEncoding()) {
      HttpSession session = req.getSession();
      AppConfiguration config = Application.getInstance(session).getConfiguration();
      req.setCharacterEncoding(config.getEncoding());
    }

    try {
      req = new EncRequestWrapper((HttpServletRequest) req, req.getCharacterEncoding());
    } catch (UnsupportedEncodingException e) {
      req = (HttpServletRequest) req;
    }

    String type = req.getHeader("Content-Type");

    // If this is not a multipart/form-data request continue
    if (type == null || !type.startsWith("multipart/form-data")) {
      chain.doFilter(req, response);
    } else {
      try {
        chain.doFilter(new MultipartWrapper(req), response);
      } catch (IllegalArgumentException e) {
        Logger.warn("MultipartFilter: "+e.getMessage());
      }
    }
  }
}