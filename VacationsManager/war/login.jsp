<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ page isELIgnored="false" %> 
<!--  <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Login</title>
</head>
<body>
	<c:if test="${not empty param.error}">
		<div style="font-size: 18; color: red;">You have entered wrong
			user info or user with such username have already been logged in.</div>
	</c:if>

	<!--<div id="login_form">
		<form action="<c:url value="/j_spring_security_check" />"
			method="post"></form>
		<label for="j_username">Логин:</label> <input type="text"
			name="j_username" id="j_username" class="login-field" /> <br /> <label
			for="j_password">Пароль:</label> <input type="password"
			name="j_password" id="j_password" class="login-field" /> <br />
		<div id="input">
			<input type="submit" value="Войти" />
		</div>
	</div>-->

	<div>
		<h2>Please, enter your username and password.</h2>
		<!--<spring:url var="authUrl" value="/j_spring_security_check" />-->
		<form method="post" class="signin" action="j_spring_security_check">
			<fieldset>
				<table cellspacing="0">
					<tr>
						<th><label for="username">Username</label></th>
						<td><input id="username" name="j_username"
							type="text" /></td>
					</tr>
					<tr>
						<th><label for="password">Password</label></th>
						<td><input id="password" name="j_password" type="password" />
							<!--<small><a href="/account/resend_password">Forgot?</a></small>-->
						</td>
					</tr>
					<!-- <tr>
						<th></th>
						<td>
							<input id="remember_me"	name="_spring_security_remember_me" type="checkbox" /> 
							<label for="remember_me" class="inline">Remember me</label>
						</td>
					</tr> -->
					<tr>
						<th></th>
						<td><input name="commit" type="submit" value="SignIn" /></td>
					</tr>
				</table>
			</fieldset>
		</form>
		<script type="text/javascript">
		document.getElementById('username').focus(); </script>

	</div>
</body>
</html>