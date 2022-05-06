$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	csrf();
	$.ajax({
		url:CONTEXT_PATH+'/discuss/addPost',
		type:'post',
		data:{
			title:$("#recipient-name").val(),
			content:$("#message-text").val(),
		},
		success:function (data){
			if(data.code==500300){
				$("#hintBody").text(data.msg);
				$("#hintModal").modal("show");
				setTimeout(function(){
					$("#hintModal").modal("hide");
					  window.location.reload();
				}, 2000);
			}else{
				console.log("未知异常");
			}
		},
		error:function (){
			console.log("评论发布失败");
		}
	})


}