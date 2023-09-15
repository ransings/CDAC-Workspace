<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>  
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Verify Signature</title>

<style type="text/css">
a {
	text-decoration: none;
	border-style: solid;
	font-weight: bold;
	padding: 3px;
	margin-top: 1%;
	border-radius: 5px;
	background-color: white;
}

.row {
	margin-top: 0%;
	display: flex;
	flex-wrap: wrap;
	margin-bottom: 2%;
}

.column {
	flex: 1;
	max-width: 33.33%;
	padding-left: 5%;
}

.column1 {
	flex: 1;
	max-width: 33.33%;
	padding-left: 6%;
}

.column2 {
	flex: 1;
	max-width: 33.33%;
	position: relative;
	padding-top: 35%;
	margin-left: -90%;
	padding-right: 5%;
}

.msg {
	margin-left: -2.5%;
	font-size: 130%;
	font-weight: bold;
}

.error {
	font-size: 125%;
	margin-left: -190%;
	margin-top: -5%;
	color: red;
	background-color: white;
	padding: 1%;
	position: relative;
}

.mainDiv {
	margin: 2% 0% 5% 20%;
}

.textLabel {
	font-size: 150%;
	padding: 4%;
	font-weight: bold;
	margin-left: -3.5%;
}

header {
	background-color: #f0eeed;
}

body {
	background-color: #f0eeed;
}

fieldset {
	background-color: white;
}

.download {
	background-color: white;
	border-radius: 20%;
	padding:3%;
	border-left-color: blue;
	border-right-color: blue;
	border-bottom-color: lime;
	border-top-color: green;
	font-size: 115%;
}

.reason {
	background-color: white;
	font-size: 130%;
	font-weight: bold;
	width:500%;
	color: red;
	margin-top: -25%;
}

.backLink{
margin-top:4%;
margin-bottom: 10%;

}

.download:hover {
	background-color: green;
	color: white;
	border-radius: 20%;
	border-left-color: silver;
	border-right-color: silver;
	border-bottom-color: white;
	border-top: none;
	font-size: 115%;
}

.footer {
	position: fixed;
	left: 0;
	bottom: 0;
	width: 100%;
	background-color: purple;
	color: white;
	text-align: center;
}
</style>

</head>
<body>

<header>
<img src="images/eSignHeaderBanner.png" width=100%>
</header>

<form action="varify_dsc" method="post">

<div class="mainDiv" >
<label class="textLabel">Response XML:</label> 
<c:choose>
<c:when test="${vflag==1}">
<label class="msg">${msg}</label>
</c:when>
</c:choose>

<br>

<textarea name="dsc" rows="20" cols="100" style="margin-top: 1%">
${dsc}
</textarea>
</div>

<div class="row">

<div class="column">
<c:if test="${vflag==1}">
<fieldset>
<legend>Signer Information</legend>
<p style="font-size: 100%">
Issued By- ${Issuer}<br><br>
Name- ${Signer_Name} <br><br>
Location- ${Location}
</p>
</fieldset>
</c:if>
</div>

<div class="column1">

<c:if test="${vflag==3}">
<center><label class="error">${error}</label>
</c:if>

<c:if test="${vflag!=3}">

<c:choose>
<c:when test="${vflag==0}">
<input type="image" src="images/failed.jpg" alt="submit" height="150"/>
</c:when>

<c:when test="${vflag==1}">
<input type="image" src="images/Pass.jpg" alt="submit" height="150"/>
</c:when>

<c:otherwise>
<input type="image" src="images/verify.jpg" alt="submit" height="150"/> 
</c:otherwise>
</c:choose>
</c:if>

</div>
</form>

<form action="cert_download" method="post">
<input type="hidden" name="dsc" value='${cert_cont}'/>
<div class="column2">
<c:if test="${vflag==1 || vflag==0}">

<c:if test="${vflag==1}">
<input type="submit" value="Download Certificate" class="download"/>
</c:if>
<c:if test="${vflag==0}">
<p class="reason" >${reason}</p>
</c:if>

</c:if>
</div>
</form>
</div>

<div class="backLink">
<center><a href="./">Back</a>
</div>

<footer class="footer">
<div>
<!-- <p>HELP &amp; CONTACT</p> -->
<!-- <hr /> -->
<!-- <img -->
<%-- src="${pageContext.request.contextPath}/resources/images/Digital_india_logo.png" --%>
<!-- alt="CDAC"></a> -->
<p>2023 C-DAC. All rights reserved | Website owned &amp;
maintained by: Centre for Development of Advanced Computing (C-DAC)</p>
</div>
</footer>

</body>
</html>
