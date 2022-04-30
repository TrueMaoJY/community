$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	csrf();
	$.ajax({
		url: CONTEXT_PATH+'/message/letter/send',
		type:'post',
		data:{
			toName:$("#recipient-name").val(),
			content:$("#message-text").val(),
		},
		success:function (data){
			if(data.code==200){
				$("#hintModal").text("发送成功");
				$("#hintModal").modal("show");
				setTimeout(function(){
					$("#hintModal").modal("hide");
					location.reload();
				}, 2000);
			}else{
				console.log(data.message);
			}
		},
		error:function (){
			console.log("请联系管理员");
		}
	})



}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}