<%@ page import="java.util.*,
                 org.snipsnap.config.Configuration"%>
 <%--
  ** Database Settings
  ** @author Matthias L. Jugel
  ** @version $Id$
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<div class="finish">
  <div><fmt:message key="config.finish"/></div>
  <c:choose>
    <c:when test="${empty advanced}">
      <script type="text/javascript" language="Javascript">
      <!--
      function enableOnCheck(checkbox) {
        document.getElementById("submit.advanced").disabled = !checkbox.checked;
        if(checkbox.name == 'advanced.all') {
          document.getElementById("advanced.moblog").disabled = checkbox.checked;
          document.getElementById("advanced.mail").disabled = checkbox.checked;
          document.getElementById("advanced.proxy").disabled = checkbox.checked;
          document.getElementById("advanced.database").disabled = checkbox.checked
        }
      }
      -->
      </script>
      <div class="advanced">
        <fmt:message key="config.advanced"/>
        <ul>
          <li>
            <input onClick="enableOnCheck(this);" id="advanced.moblog" type="checkbox" name="advanced.step.moblog">
            <fmt:message key="config.advanced.moblog"/>
          </li>
          <li>
            <input onClick="enableOnCheck(this);" id="advanced.mail" type="checkbox" name="advanced.step.mail">
            <fmt:message key="config.advanced.mail"/>
          </li>
          <li>
            <input onClick="enableOnCheck(this);" id="advanced.proxy" type="checkbox" name="advanced.step.proxy">
            <fmt:message key="config.advanced.proxy"/>
          </li>
          <li>
            <input onClick="enableOnCheck(this);" id="advanced.database" type="checkbox" name="advanced.step.database">
            <fmt:message key="config.advanced.database"/>
          </li>
          <li>
            <input onClick="enableOnCheck(this);" type="checkbox" name="advanced.all">
            <fmt:message key="config.advanced.all"/>
          </li>
        </ul>
      </div>
    </c:when>
    <c:otherwise>
      <div class="advanced"><fmt:message key="config.advanced.forgot"/></div>
    </c:otherwise>
 </c:choose>
</div>