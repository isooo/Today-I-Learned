package item35

class Dialog {
    var title: String = ""
    var text: String = ""
    fun show() { /*...*/
    }
}

fun main() {
    // 일반적인 방법: dialog 변수를 반복해서 사용해야하는 귀찮음이 있음..
    val dialog1 = Dialog()
    dialog1.title = "My dialog"
    dialog1.text = "Some text"
    dialog1.show()

    // lambda expression with receiver를 사용한 방법: this를 이용해 수신 객체를 암묵적으로 사용
    val dialog2 = Dialog()
    val init: Dialog.() -> Unit = {
        title = "My dialog"
        text = "Some text"
    }
    init.invoke(dialog2)
    dialog2.show()

    // 특정 부분(Dialog 생성 부분 및 show() 호출)을 공통화하여, 필요한 부분(프로퍼티 세팅)만 세팅할 수 있게 한 방법
    showDialog {
        title = "My dialog"
        text = "Some text"
    } // --> 이 방식이 DSL!!!

    // apply function 사용: 위에서 직접 정의한 DSL 빌더는, apply 사용으로 대체할 수 있다
    Dialog().apply {
        title = "My dialog"
        text = "Some text"
    }.show()
}

fun showDialog(init: Dialog.() -> Unit) {
    val dialog = Dialog()
    init.invoke(dialog)
    dialog.show()
}
