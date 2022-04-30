$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	csrf();
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.ajax({
			url:CONTEXT_PATH+'/follow',
			type:'post',
			data:{
				entityId:$("#entityId").val(),
				entityType:3,
			},
			success:function (){
				// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
				 window.location.reload();

			}
		})

	} else {
		// 取消关注
		$.ajax({
			url:CONTEXT_PATH+'/unfollow',
			type:'post',
			data:{
				entityId:$("#entityId").val(),
				entityType:3,
			},
			success:function (){
				// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
				 window.location.reload();

			}
		})

	}
}