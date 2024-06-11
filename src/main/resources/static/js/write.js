$(function(){
    // [추가] 버튼 누르면 첨부 파일 추거 기능
    let i = 0;
    $("#btnAdd").click(function (){
        $("#files").append(`
            <div class="input-group mb-2">
                <input class="form-control col-xs-3" type="file" name="upfile${i}"/>    <!-- 각 첨부파일에 다른 name 부여 -->
                <button type="button" class="btn btn-outline-danger" onclick="$(this).parent().remove()">삭제</button>
            </div>`);
        i++;
    });

    // Summernote 추가
    $("#content").summernote({
        height: 300,
    });
});