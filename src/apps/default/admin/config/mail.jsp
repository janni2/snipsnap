<%@ page import="java.util.*,
                 org.snipsnap.config.Configuration"%>
 <%--
  ** Mail Settings
  ** @author Matthias L. Jugel
  ** @version $Id$
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<table>
  <tr>
    <td><fmt:message key="config.app.mail.host.text"/></td>
    <td>
      <fmt:message key="config.app.mail.host"/><br/>
      <input type="text" name="app.mail.host" size="40" value="<c:out value='${config.mailHost}'/>">
      <c:if test="${!empty errors['app.mail.host']}"><img src="images/attention.jpg"></c:if>
    </td>
  </tr>
  <tr>
    <td><fmt:message key="config.app.mail.domain.text"/></td>
    <td>
      <fmt:message key="config.app.mail.domain"/><br/>
      <input type="text" name="app.mail.domain" size="40" value="<c:out value='${config.mailDomain}'/>">
      <c:if test="${!empty errors['app.mail.domain']}"><img src="images/attention.jpg"></c:if>
    </td>
  </tr>
</table>
