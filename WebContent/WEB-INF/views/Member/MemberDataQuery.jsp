<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>會員基本資料查詢</title>

<jsp:include page="../../views/Common/CommonResource.jsp"></jsp:include>
<!-- 
<link href="../../Content/Site.css" rel="stylesheet" type="text/css" />
<link href="../../themes/${userBean.theme}/jquery-ui-1.8.7.custom.css" rel="stylesheet" type="text/css" />
<link href="../../Content/JQueryUICustom.css" rel="stylesheet" type="text/css" />
<link href="../../Content/ui.combobox.css" rel="stylesheet" type="text/css" />
<script src="../../Script/jquery-1.4.1.js" type="text/javascript"></script>
<script src="../../Script/jquery-ui-1.8.7.custom.min.js" type="text/javascript"></script>
<script src="../../Script/jquery.blockUI.js" type="text/javascript"></script>
<script src="../../Script/jquery.gcms.js" type="text/javascript"></script>
<script src="../../Script/jquery.combobox.js" type="text/javascript"></script>
 -->
 
 <script>
	
	$.fx.speeds._default = 1000;
	$(function() {
		$("#submit").button();
		
		// Create Btn
		$("#createBtn").button({
            icons: {
                primary: "ui-icon-circle-plus"
            }
        }).click(function () {

			
        	
        	
        });
		
		// Read Btn
		$("#readBtn").button({
            icons: {
                primary: "ui-icon-search"
            }
        }).click(function () {
            $.blockUI({ message: '<div>載入資料中...</div>', overlayCSS: { backgroundColor: '#4297D7'} });
            readData();
        });
		
		// Delete Btn
		$("#deleteBtn").button({
            icons: {
                primary: "ui-icon-circle-close"
            }
        }).click(function () {
            $.blockUI({ message: $('#question'), css: { width: '275px'} });
            
            
        });
		
		var oTable = $('#jtable').dataTable({
            //"sScrollY":  '100%',
            "bJQueryUI": true,
            "sPaginationType": "full_numbers"
        });
		
		readData();
	});
	
	
    function readData() {

        $.getJSON('/funbackend/controller/Mqtt/ReadMessage', function (data) {

            $('#jtable').dataTable().fnClearTable(true);
            
            $.each(data, function (k, v) {
            
                $('#jtable').dataTable().fnAddData([
	                v.serial,
	                v.target,
	                v.message 
                ]);
            });

            $.unblockUI();
        });
    }
	
</script>

</head>
<body>
	<fieldset>
		<legend>會員基本資料查詢</legend>
		<p />
		<form action="CreateMessage" method="post">
			<label>發送目標:</label><input id="target" name="target" type="text"
				value="" size="20" /><br />
			<br /> <label>發送訊息:</label><input id="message" name="message"
				type="text" value="" size="20" /><br />
			<br /> <input id="submit" name="submit" type="submit" value="發送" /><br />
			<br />
		</form>
	</fieldset>




</body>
</html>