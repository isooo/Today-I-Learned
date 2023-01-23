package item36

class ProfileLoader {
    fun load () {
        // show progress bar
        // load profile
        // hide progress bar
    }
}

class ImageLoader {
    fun load () {
        // show progress bar
        // load image
        // hide progress bar
    }
}

abstract class LoaderWithProgressBar {
    fun load () {
        // show progress bar
        action()
        // hide progress bar
    }

    abstract fun action()
}

class ProfileLoader2 : LoaderWithProgressBar() {
    override fun action() {
        // load profile
    }
}
class ImageLoader2 : LoaderWithProgressBar() {
    override fun action() {
        // load profile
    }
}
