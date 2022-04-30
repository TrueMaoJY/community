$(function (){
    csrf();
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#blockBtn").click(setBlock);

});
function setTop(){
    $.ajax({
        url:CONTEXT_PATH+'/discuss/top',
        type:'post',
        data:{
            id:$("#postId").val(),
        },
        success:function (data){
            if (data.code==200){
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(data.message);
            }
        }
    })
}

function setWonderful(){
    $.ajax({
        url:CONTEXT_PATH+'/discuss/wonderful',
        type:'post',
        data:{
            id:$("#postId").val(),
        },
        success:function (data){
            if (data.code==200){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else{
                alert(data.message)
            }
        }
    })
}
function setBlock(){
    $.ajax({
        url:CONTEXT_PATH+'/discuss/block',
        type:'post',
        data:{
            id:$("#postId").val(),
        },
        success:function (data){
            if (data.code==200){
               window.location.href=CONTEXT_PATH+"/index";
            } else{
                alert(data.message)
            }
        }
    })
}