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
package org.snipsnap.admin.install;

import org.snipsnap.snip.SnipLink;
import org.snipsnap.config.Configuration;
import org.snipsnap.config.AppConfiguration;
import org.snipsnap.admin.CommandHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Loads the list of installed applications.
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class Applications extends HttpServlet {
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if(session != null) {
      Configuration config = (Configuration)session.getAttribute(CommandHandler.ATT_CONFIG);
      String root = config.getProperty(Configuration.SERVER_WEBAPP_ROOT);
      Map applications = new HashMap();
      File dir = new File(root);
      if(dir.isDirectory()) {
        File[] apps = dir.listFiles();
        for(int i = 0; i < apps.length; i++) {
          try {
            AppConfiguration appConfig = loadApp(apps[i]);
            String name = appConfig.getName();
            applications.put(name != null ? name : apps[i].getName(), appConfig);
          } catch (IOException e) {
            System.err.println("ignoring: "+e.getMessage());
          }
        }
      }
      // forward to list of applications or direct install if none exist
      if(applications.size() > 0) {
        session.setAttribute("serverApplications", applications);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/welcome.jsp");
        dispatcher.forward(request, response);
      } else {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/install.jsp");
        dispatcher.forward(request, response);
      }
    } else {
      response.sendRedirect(SnipLink.absoluteLink(request, "/"));
    }
  }

  private AppConfiguration loadApp(File dir) throws IOException {
    File configFile = new File(dir, "application.conf");
    if(configFile.exists() && !configFile.isDirectory()) {
      return new AppConfiguration(configFile);
    } else {
      throw new IOException(configFile.getAbsolutePath()+" is not a configuration file");
    }
  }
}