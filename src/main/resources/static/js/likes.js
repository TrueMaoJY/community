
    function likes(btn,entityType,entityId,entityUserId,postId){
    csrf();
    $.ajax({
        url:CONTEXT_PATH+"/like",
        type:'post',
        data:{
            entityId:entityId,
            entityType:entityType,
            entityUserId:entityUserId,
            postId:postId
        },
        success:function (data){
            if(data.code==200){
                $(btn).children("i").text(data.obj.likeCount);
                $(btn).children("b").text(data.obj.likeStatus==0?"赞":"已赞");
            }
        }

    })
}
