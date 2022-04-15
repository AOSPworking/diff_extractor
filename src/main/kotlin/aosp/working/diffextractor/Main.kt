package aosp.working.diffextractor

import aosp.working.diffextractor.dto.FileProperty
import aosp.working.diffextractor.dto.TopJson
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.github.gumtreediff.actions.EditScript
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.reflect.KProperty

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        //test()
        assert(args.size > 3) {
            "The 1st Argument is path of repository\n" +
                    "The 2nd Argument is one commit id\n" +
                    "The 3rd Argument is another commit id\n"
        }

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
            val fileName: String = JGitUtil.getNeedPathFromDiffEntry(diffEntry)
            val methods: List<String> = editScript
                .filter { gumtreeUtil.isTreeAccessDownToUp(it.node, "MethodDeclaration") }
                .map { GumtreeUtil.getFullyQualifiedMethodName(it.node)!! }
                .distinct()
            FileProperty(fileName, methods)
        }

        val topJson = TopJson(firstCommitId, secondCommitId, fileProperties)
        val result = Klaxon().toJsonString(topJson)
        Files.writeString(Path.of(Global.props["extract.output.path"] as String), result)
    }
}