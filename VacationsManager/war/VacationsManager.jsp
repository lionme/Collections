<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglibprefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">

<!--                                                               -->
<!-- Consider inlining CSS to reduce the number of requested files -->
<!--                                                               -->
<link type="text/css" rel="stylesheet" href="VacationsManager.css">

<!--                                           -->
<!-- Any title is fine                         -->
<!--                                           -->
<title>Vacation Manager</title>

<!--                                           -->
<!-- This script loads your compiled module.   -->
<!-- If you add any GWT meta tags, they must   -->
<!-- be added before this line.                -->
<!--                                           -->
<script type="text/javascript" language="javascript"
	src="vacationsmanager/vacationsmanager.nocache.js"></script>
</head>

<!--                                           -->
<!-- The body can have arbitrary html, or      -->
<!-- you can leave the body empty if you want  -->
<!-- to create a completely dynamic UI.        -->
<!--                                           -->
<body>

	<!-- OPTIONAL: include this if you want history support -->
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
		style="position: absolute; width: 0; height: 0; border: 0"></iframe>

	<!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
	<noscript>
		<div
			style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
			Your web browser must have JavaScript enabled in order for this
			application to display correctly.</div>
	</noscript>
	
	<!-- Table for top change password button and redirect link -->
	<table width="100%">
		<tr>
			<td id="changePasswordButtonContainer" width="50%" align="left" />
			<td id="logOffLinkContainer" width="50%" align="right">
				<span style="margin-right: 50px;"> Your username: <b><security:authentication property="principal.username"/></b></span>
				<a href="<c:url value="/logout" />"> Logout</a>
			</td>
		</tr>
	</table>

	<!-- <div align="right">
		<span style="margin-right: 50px;"> Your username: <b><security:authentication property="principal.username"/></b></span>
		<a href="<c:url value="/logout" />"> Logout</a>
	<div> -->

	<!-- This part appears on the web page only for users with admin rights. -->
	<security:authorize access="hasAdminRights()">
		<table align="right">
			<tr>
				<td id="isAdminContainer"></td>
			</tr>
		</table>
	</security:authorize>
	
	<table align="center">
		<tr>
			<td colspan="2" id="vacationsListContainer"></td>
	    </tr>
		<tr>
			<td id="vaitingOnApprovalContainer" width="50%" valign="top"></td>
			<td id="remainingVacationDaysContainer" width="50%" valign="top"></td>
		</tr>
		<!-- <tr>
			<td colspan="2" style="color: red;" id="errorLabelContainer"></td>
		</tr>-->
	</table>
</body>
</html>