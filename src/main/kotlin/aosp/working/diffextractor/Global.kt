package aosp.working.diffextractor

import org.eclipse.jgit.lib.Repository
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

object Global {
    val props: Properties = Properties()
    private val repository: Repository
    val jGitUtil: JGitUtil

    init {
        FileInputStream(File("config.properties")).use { this.props.load(it) }
        this.repository = JGitUtil.makeRepository(props["extract.input.repo"] as String)
        this.jGitUtil = JGitUtil(this.repository)
    }
}