package aosp.working.diffextractor

import com.github.gumtreediff.tree.Tree
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.io.ByteArrayOutputStream

import java.io.File

class JGitUtil(val repository: Repository) {

    constructor(repoPath: String): this(makeRepository(repoPath))

    /**
     * 根据一个 commit，获得其与先前 parent commit 间所有存在 diff 的文件名。
     * @param commit
     * @return
     */
    fun getChangeFilesByCommit(commit: RevCommit): List<String> {
        val result: MutableSet<String> = mutableSetOf()
        val diffMap = this.getParentMappedDiffEntry(commit)
        for ((_, diffEntries) in diffMap) {
            diffEntries.forEach { diffEntry ->
                getNeedPathFromDiffEntry(diffEntry)
                    .let { if (it !in result) result.add(it) }
            }
        }
        return result.toList()
    }

    /**
     * 根据两个 commit，获得其间的差异，并且获得差异相关的所有文件名。
     * 这两个传入的 commit 间没有 parent 关系，任意两个都可以。
     * @param curr 当前 commit
     * @param prev 另一个 commit
     */
    fun getChangeFilesByCommit(curr: RevCommit, prev: RevCommit): List<String> {
        val result: MutableSet<String> = mutableSetOf()
        val entries = this.getParentMappedDiffEntry(curr, prev)
        entries.forEach { entry -> getNeedPathFromDiffEntry(
            entry
        ).let {
            if (it !in result) result.add(it)
        } }
        return result.toList()
    }

    /**
     * 传入一个 RevCommit，然后获得他的所有 DiffEntry
     * 直接传入 RevCommit 能够避免 RevWalk 在 parseCommit 时带来的开销，极大提高性能
     * @param curr
     * @return Map key 是 parent 的 commit，value 是那个 commit 对应的 diff
     */
    fun getParentMappedDiffEntry(curr: RevCommit): Map<RevCommit, List<DiffEntry>> {
        val result: MutableMap<RevCommit, List<DiffEntry>> = mutableMapOf()
        curr.parents.forEach { result[it] = this.getParentMappedDiffEntry(curr, it) }
        return result
    }

    /**
     * 根据 current 和 previous 获取 List<DiffEntry>
     * @param curr 当前的 commit
     * @param prev 过去的某个 commit，一般来说是 current commit 的其中一个 parent
     */
    fun getParentMappedDiffEntry(curr: RevCommit, prev: RevCommit): List<DiffEntry> {
        val reader: ObjectReader = this.repository.newObjectReader()
        val newTree: ObjectId = curr.tree.id
        val oldTree: ObjectId = prev.tree.id
        val newTreeIter = CanonicalTreeParser()
        val oldTreeIter = CanonicalTreeParser()

        DiffFormatter(DisabledOutputStream.INSTANCE).use {
            it.setRepository(this.repository)
            newTreeIter.reset(reader, newTree)
            oldTreeIter.reset(reader, oldTree)
            return it.scan(oldTreeIter, newTreeIter)
        }
    }

    /**
     * 根据 commit 的 hash id 获得对应的 RevCommit
     * @param commitId 传入的 commit hash id
     */
    fun parseRevCommit(commitId: String): RevCommit {
        return this.repository.parseCommit(this.repository.resolve(commitId))
    }

    /**
     * 获得某 commit 下的某个名为 fileName 的文件
     * @param fileName 文件名
     * @param commit
     */
    fun extract(fileName: String, commit: RevCommit): ByteArray
        = TreeWalk.forPath(this.repository, fileName, commit.tree).use {
            val objectId = it.getObjectId(0)
            val objectLoader: ObjectLoader = this.repository.open(objectId)
            val os = ByteArrayOutputStream()
            objectLoader.copyTo(os)
            os.toByteArray() }

    companion object {
        /**
         * 给定一个文件系统中的 repo .git 路径，得到一个 Repository 对象。
         * @param repoPath
         */
        fun makeRepository(repoPath: String): Repository
            = RepositoryBuilder()
                .setGitDir(File(repoPath))
                .readEnvironment()
                .findGitDir()
                .build()

        /**
         * 获得在 diff 中需要的路径。
         * @param diffEntry
         */
        fun getNeedPathFromDiffEntry(diffEntry: DiffEntry): String
            = when (diffEntry.changeType) {
                DiffEntry.ChangeType.ADD,
                DiffEntry.ChangeType.COPY,
                DiffEntry.ChangeType.RENAME -> diffEntry.newPath
                DiffEntry.ChangeType.MODIFY,
                DiffEntry.ChangeType.DELETE -> diffEntry.oldPath
            }


    }
}
