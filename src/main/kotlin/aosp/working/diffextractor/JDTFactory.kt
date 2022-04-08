package aosp.working.diffextractor

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import java.io.File

object JDTFactory {
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
}