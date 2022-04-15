package aosp.working.diffextractor

import com.github.gumtreediff.tree.Tree
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jgit.revwalk.RevCommit
import java.io.ByteArrayInputStream
import java.io.File

object JDTUtil {
    private val options: MutableMap<String, String> = JavaCore.getOptions()

    init {
        options[JavaCore.COMPILER_COMPLIANCE] = JavaCore.VERSION_1_8
        options[JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM] = JavaCore.VERSION_1_8
        options[JavaCore.COMPILER_SOURCE] = JavaCore.VERSION_1_8
        options[JavaCore.COMPILER_DOC_COMMENT_SUPPORT] = JavaCore.ENABLED
    }

    /**
     * 根据 java 文件路径，获得一个 cu。
     * @param filePath
     */
    fun make(filePath: String): CompilationUnit
        = ASTParser.newParser(AST.JLS8).let {
            it.setKind(ASTParser.K_COMPILATION_UNIT)
            it.setCompilerOptions(options)
            it.setSource(File(filePath).inputStream().readBytes().toString(Charsets.UTF_8).toCharArray())
            it.createAST(null) } as CompilationUnit

    /**
     * 根据 java 文件 ByteArray，获得一个 cu。
     * @param byteArray
     */
    fun make(byteArray: ByteArray): CompilationUnit
        = ASTParser.newParser(AST.JLS8).let {
        it.setKind(ASTParser.K_COMPILATION_UNIT)
        it.setCompilerOptions(options)
        it.setSource(byteArray.toString(Charsets.UTF_8).toCharArray())
        it.createAST(null) } as CompilationUnit

    /**
     * 获得某 commit 下的某个名为 fileName 的文件对应的 cu
     * @param fileName 文件名
     * @param commit
     */
    fun make(fileName: String, commit: RevCommit): CompilationUnit
            = make(Global.jGitUtil.extract(fileName, commit))
}