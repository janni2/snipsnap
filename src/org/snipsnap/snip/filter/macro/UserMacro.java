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
/*
 * Macro that displays all Snips by user
 *
 * @author stephan
 * @version $Id$
 */

package com.neotis.snip.filter.macro;

import com.neotis.snip.Snip;
import com.neotis.snip.SnipSpace;
import com.neotis.snip.SnipLink;
import com.neotis.user.UserManager;
import com.neotis.user.User;

import java.util.Iterator;
import java.util.List;

public class UserMacro extends Macro {
  StringBuffer buffer;

  public UserMacro() {
    buffer = new StringBuffer();
  }

  public String execute(String[] params, String content, Snip snip) throws IllegalArgumentException {
    if (params.length == 0) {
      buffer.setLength(0);
      buffer.append("<b>user: (");
      List users = UserManager.getInstance().getAll();
      buffer.append(users.size());
      buffer.append(") </b><br/>");
      if (users.size() > 0) {
        buffer.append("<blockquote>");
        Iterator userIterator = users.iterator();
        while (userIterator.hasNext()) {
          User user = (User) userIterator.next();
          SnipLink.appendLink(buffer, user.getLogin());
          if (userIterator.hasNext()) {
            buffer.append(", ");
          }
        }
        buffer.append("</blockquote>");
      } else {
        buffer.append("no users. not very popular ;-)");
      }
      return buffer.toString();
    } else {
      throw new IllegalArgumentException("Number of arguments does not match");
    }
  }
}