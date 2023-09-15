<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>404 Not Found</title>

<style type="text/css">
header{
background-color: #f0eeed;
height: 30%;
}
body{
background-color: #f0eeed;
}
img{
width: 100%;
height: 62%;
}
h2{
color: red;
font-size: 30px;
margin-bottom: 6%;
margin-top: 4%;
}
label{
font-size: 20px;
font-family: monospace;
font-weight: bold;
}
.footer {
position: fixed;
left: 0;
bottom: 0;
height:8%;
width: 100%;
background-color: purple;
color: white;
text-align: center;
}
</style>
</head>
<header>
<img src="images/eSignHeaderBanner.png" width=100%>
</header>

<body>
<center><h2>404 NOT FOUND!</h2></center>
<center><label>Unable to find requested resource</label></center>

</body>

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

</html>