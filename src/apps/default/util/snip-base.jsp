<%--
  ** Displays the snip content if it's not a weblog and the header
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://snipsnap.com/snipsnap" prefix="s" %>

<%-- Snip header, displayed only when snip is not a weblog --%>
<c:if test="${snip.notWeblog}">
 <div id="snip-title">
  <h1 class="snip-name"><c:out value="${snip.name}"/></h1>
  <c:if test="${snip.comment}">
   <span class="snip-commented-snip"><s:image name="arrow"/> <a href="../comments/<c:out value='${snip.commentedSnip.nameEncoded}'/>"><c:out value='${snip.commentedSnip.name}'/></a></span>
  </c:if>
  <div id="snip-buttons"><c:import url="util/buttons.jsp"/></div>
  <div id="snip-info"><c:out value="${snip.modified}" escapeXml="false"/> Viewed <c:out value="${snip.access.viewCount}"/> times.</div>
 </div>
</c:if>
<%-- Snip content --%>
<div id="snip-content"><c:out value="${snip.XMLContent}" escapeXml="false" /></div>