<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://snipsnap.com/snipsnap" prefix="s" %>

<%-- [<a href="http://www.google.com/search?q=<c:out value='${snip.nameEncoded}'/>">google</a>]
[<a href="http://www.daypop.com/search?q=<c:out value='${snip.nameEncoded}'/>">daypop</a>] --%>
<c:if test="${snip.version > 1}">
  [<a href="exec/diff?name=<c:out value='${snip.nameEncoded}'/>&amp;oldVersion=<c:out value="${snip.version-1}"/>&amp;newVersion=<c:out value="${snip.version}"/>"><fmt:message key="menu.diff"/></a>]
  [<a href="exec/history?name=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.history"/></a>]
</c:if>
<s:check permission="REMOVE_SNIP" context="${snip}">[<a href="exec/remove?name=<c:out value='${snip.nameEncoded}'/>" onClick="return confirm('<fmt:message key="dialog.deleteSnipSure"/>');"><fmt:message key="menu.delete"/></a>]</s:check>
<s:check permission="VIEW_RAW_SNIP" context="${snip}">[<a href="raw/<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.view"/></a>]</s:check>
<s:check permission="EDIT_SNIP" context="${snip}">[<a href="exec/edit?name=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.edit"/></a>]</s:check>
<s:check permission="CREATE_SNIP" context="${snip}">[<a href="exec/new?parent=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.new"/></a>]</s:check>
<s:check permission="COPY_SNIP" context="${snip}">[<a href="exec/copy?snip=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.copy"/></a>]</s:check>
[<a href="rdf/<c:out value='${snip.nameEncoded}'/>">rdf</a>]
<%-- keep extra --%>
<s:check permission="VIEW_PERMISSIONS" context="${snip}"><div class="permissions"><c:out value="${snip.permissions}"/></div></s:check>
