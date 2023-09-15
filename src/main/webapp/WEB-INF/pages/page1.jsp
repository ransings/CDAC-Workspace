
<%@page isELIgnored="false" %>
<html>
<body>
<script>
window.onload = function() {
    document.getElementById("myform").submit();
}
</script>

<form id="myform" action='${url}' method="Post">
<input type="hidden" name="eSignRequest" value='${esignRequest}'>
<input type="hidden" name="aspTxnID" value='${txnId}'>
<input type="hidden" name="Content-Type" value='${content}'>
</form>
</body>

</html>