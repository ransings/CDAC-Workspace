<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Post Request</title>
</head>
<body>
<script>
window.onload = function() {
    document.getElementById("myform").submit();
}
</script>

<form id="myform" action='${url}' method="Post">
<input type="hidden" name="txnref" value='${txnref}'>
</form>
</body>
</html>