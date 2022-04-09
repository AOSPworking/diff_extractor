package aosp.working.diffextractor

import aosp.working.diffextractor.tree.ModifyEntryTreeComparer
import com.github.gumtreediff.actions.EditScript
import com.github.gumtreediff.actions.EditScriptGenerator
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator
import com.github.gumtreediff.client.Run
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator
import com.github.gumtreediff.tree.Tree
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class GumtreeUtil(repoPath: String) {
    private val editScriptGenerator: EditScriptGenerator = SimplifiedChawatheScriptGenerator()
    private val jGitUtil = JGitUtil(repoPath)

    init {
        Run.initGenerators()
    }

    /**
     * 根据两个 RevCommit 获得 EditScript。
     */
    fun getDiffEntryToEditScriptMapByTwoCommit(curr: RevCommit, prev: RevCommit): Map<DiffEntry, EditScript> {
        val result: HashMap<DiffEntry, EditScript> = hashMapOf()
        val entries: List<DiffEntry> = this.jGitUtil.getParentMappedDiffEntry(curr, prev)
        for (entry in entries) {
            val treeComparer = this.getTreeComparerByDiffEntry(entry, curr, prev)
            if (treeComparer != null) {
                result[entry] = treeComparer.compare()
            }
        }
        return result
    }

    /**
     * 根据 DiffEntry 和 diff 的两侧 RevCommit 获得 TreeComparer。
     * @return 如果不是 MODIFY 就返回 null。
     *         因为 RENAME 和 COPY 很少出现；并且 ADD 和 DELETE 无法获得两棵树，GumTree 无法处理空树。
     */
    fun getTreeComparerByDiffEntry(entry: DiffEntry, curr: RevCommit, prev: RevCommit)
        = when (entry.changeType) {
            DiffEntry.ChangeType.MODIFY -> ModifyEntryTreeComparer(
                this.getTreeByByteArray(jGitUtil.extract(JGitUtil.getNeedPathFromDiffEntry(entry), prev)),
                this.getTreeByByteArray(jGitUtil.extract(JGitUtil.getNeedPathFromDiffEntry(entry), curr))
            )
            else -> null
        }

    /**
     * 根据文件的 ByteArray 获得对应的 GumTree。
     * @param bytes 文件读取后的 byte[]，
     * @return 自顶向下的 GumTree，从根节点开始。
     */
    fun getTreeByByteArray(bytes: ByteArray): Tree =
        InputStreamReader(ByteArrayInputStream(bytes)).use {
            JavaParserGenerator().generateFrom().reader(it).root
        }

    /**
     * 判断该 tree 是否可以自底向上访问到以 label 为标签的直接 parent 节点。
     * @param tree 节点
     * @param label 确定想要访问的 label
     */
    fun isTreeAccessDownToUp(tree: Tree, label: String): Boolean {
        var iterTree = tree
        while (iterTree.parent != null && iterTree.type?.name != label) {
            iterTree = iterTree.parent
            if (iterTree.type?.name == label) return true
        }
        return false
    }

    /**
     * 根据当前的 Tree 节点，从下往上找到它 MethodDeclaration 的远亲节点。
     * 并且获得 MethodDeclaration 下的 SimpleName (方法名)。
     * @param tree 传入的 Tree 节点，注意这不是树，而是树上的一个节点。
     * @return 找不到就返回 null
     */
    fun findMethodNameOfDiffEntry(tree: Tree): String? {
        var iterTree = tree
        var className: String? = null; var methodName: String? = null
        while (iterTree.parent != null && iterTree.type?.name != "MethodDeclaration") {
            iterTree = iterTree.parent
        }
        for (child in iterTree.children) {
            if (child.type.name == "SimpleName") methodName = child.label
        }
        if (methodName == null) return null

        while (iterTree.parent != null && iterTree.type?.name != "ClassOrInterfaceDeclaration") {
            iterTree = iterTree.parent
        }
        for (child in iterTree.children) {
            if (child.type.name == "SimpleName") className = child.label
        }
        if (className == null) return null
        return "$className.$methodName"
    }

    companion object {
        fun getEmptyTree(): Tree =
            InputStreamReader(ByteArrayInputStream(byteArrayOf())).use {
                JavaParserGenerator().generateFrom().reader(it).root
            }
    }
}