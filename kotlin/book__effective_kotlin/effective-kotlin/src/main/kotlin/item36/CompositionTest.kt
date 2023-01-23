package item36

class ProgressBar {
    fun show() { /* show progress bar */ }
    fun hide() { /* hide progress bar */ }
}

class ProfileLoader3 {
    val progressBar = ProgressBar()
    fun load() {
        progressBar.show()
        // load profile
        progressBar.hide()
    }
}
class ImageLoader3 {
    val progressBar = ProgressBar()
    fun load() {
        progressBar.show()
        // load image
        progressBar.hide()
    }
}

