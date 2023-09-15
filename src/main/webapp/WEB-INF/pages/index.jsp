<html>
<head>
<style type="text/css">

.p {
	text-align: center;
	font-size: 200%;
	color: fuchsia;
	margin-left:6%;
	color:black;
	font-weight: bold;
}
.text{
	height: 70%;
	width: 40%;
	rows:50;
	margin-left:33%;
	margin-top:0.3%;
}
.center{
text-align: center;
}
input[type=checkbox]{
height: 3%;
width: 3%;
margin-top: 0%;
margin-left:-3%;
position: absolute;
}
.consent{
margin:2% 15% 0% 24%;
padding-right: 30px;
}
.submit{
background-color: green;
color:white;
font-size: 130%;
font-weight:300;
height: 5.5%;
width: 7.5%;
border-radius: 9%;
border-color:white;
margin-left: 3%;
margin-bottom: 5%;
}
.reset{
background-color: green;
color:white;
font-size: 130%;
font-weight:300;
height: 5.5%;
width: 7.5%;
border-radius: 9%;
border-color:white;
margin-left: 44%;
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
header{
background-color: #f0eeed;
}
body{
background-color: #f0eeed;
}

</style>
</head>

<body>

<header>
<img src="images/eSignHeaderBanner.png" width=100%>
</header>

<div>

<form action="https://10.208.55.181:8080/Esign/validate" method="post">
<p class="p">Xml/String Signing</p>

<div >
<div>
<textarea name="data" class="text" required="required"></textarea>
<br><br>
</div>
<div class="consent center">
    <input type="checkbox" required />
  <label>  By clicking the checkbox, I hereby give my consent for using e-KYC services data from AADHAAR for the purpose of signing selected document and generating Digital signature.
  </label>
 </div>
 <br>
 <input type="submit" value="eSign" class="reset"/>
 <input type="reset" value="Clear" class="submit"/>
</div>
</form>

</div>

<footer class="footer">
<div class="container">
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