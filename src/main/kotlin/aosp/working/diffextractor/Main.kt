package aosp.working.diffextractor

import com.beust.klaxon.Klaxon
import com.github.gumtreediff.actions.EditScript
import com.github.gumtreediff.actions.EditScriptGenerator
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator
import com.github.gumtreediff.matchers.Matchers
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.*
import kotlin.collections.HashMap

object Main {
    @JvmStatic
    fun test() {
        val file = File("output.json")
        val str = file.readText()
        val json = Klaxon().parse<TopJson>(str)
        println(json)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        //test()
        assert(args.size > 3) {
            "The 1st Argument is path of repository\n" +
                    "The 2nd Argument is one commit id\n" +
                    "The 3rd Argument is another commit id\n"
        }

        val configFile = File("config.properties")
        val props = Properties()
        FileInputStream(configFile).use { props.load(it) }

        val repoPath: String = args[0]
        val util = JGitUtil(repoPath)
        val gumtreeUtil = GumtreeUtil(repoPath)

        val firstCommitId: String = args[1]
        val secondCommitId: String = args[2]
        val firstCommit: RevCommit = util.parseRevCommit(firstCommitId)
        val secondCommit: RevCommit = util.parseRevCommit(secondCommitId)

        val editScripts: Map<DiffEntry, EditScript> =
            gumtreeUtil.getDiffEntryToEditScriptMapByTwoCommit(firstCommit, secondCommit)

        val fileProperties: List<FileProperty> = editScripts.map { (diffEntry, editScript) ->
            val methods: List<String> = editScript
                .filter { gumtreeUtil.isTreeAccessDownToUp(it.node, "MethodDeclaration") }
                .map { gumtreeUtil.findMethodNameOfDiffEntry(it.node)!! }
                .distinct()
            FileProperty(JGitUtil.getNeedPathFromDiffEntry(diffEntry), methods)
        }

        val commitChangeFiles: HashMap<String, List<FileProperty>> = hashMapOf()
        commitChangeFiles[secondCommitId] = fileProperties

        val topJson = TopJson(commitChangeFiles)
        val result = Klaxon().toJsonString(topJson)
        println(result)
    }
}