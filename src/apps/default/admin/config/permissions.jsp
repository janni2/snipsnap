<%@ page import="java.util.*,
                 org.snipsnap.config.Configuration"%>
 <%--
  ** Permission settings
  ** @author Matthias L. Jugel
  ** @version $Id$
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<table>
  <tr>
    <td><fmt:message key="config.app.perm.register.text"/></td>
    <td>
      <fmt:message key="config.app.perm.register"/><br/>
      <input type="checkbox" name="app.perm.register" value="allow"
        <c:if test="${config.permRegister == 'allow'}">checked="checked"</c:if>>
    </td>
  </tr>
  <tr>
    <td><fmt:message key="config.app.perm.weblogsPing.text"/></td>
    <td>
      <fmt:message key="config.app.perm.weblogsPing"/><br/>
      <input type="checkbox" name="app.perm.weblogsPing" value="allow"
        <c:if test="${config.permWeblogsPing == 'allow'}">checked="checked"</c:if>>
    </td>
  </tr>
    <tr>
    <td><fmt:message key="config.app.perm.notification.text"/></td>
    <td>
      <fmt:message key="config.app.perm.notification"/><br/>
      <input type="checkbox" name="app.perm.notification" value="allow"
        <c:if test="${config.permNotification == 'allow'}">checked="checked"</c:if>>
    </td>
  </tr>
  <tr>
    <td><fmt:message key="config.app.perm.externalImages.text"/></td>
    <td>
      <fmt:message key="config.app.perm.externalImages"/><br/>
      <input type="checkbox" name="app.perm.externalImages" value="allow"
        <c:if test="${config.permExternalImages == 'allow'}">checked="checked"</c:if>>
    </td>
  </tr>
    <tr>
    <td><fmt:message key="config.app.perm.multiplePosts.text"/></td>
    <td>
      <fmt:message key="config.app.perm.multiplePosts"/><br/>
      <input disabled="disabled" type="checkbox" name="app.perm.multiplePosts" value="allow"
        <c:if test="${config.permMultiplePosts == 'allow'}">checked="checked"</c:if>>
    </td>
  </tr>
</table>