package aosp.working.diffextractor.tree

import com.github.gumtreediff.actions.EditScript
import com.github.gumtreediff.actions.EditScriptGenerator
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator
import com.github.gumtreediff.matchers.MappingStore
import com.github.gumtreediff.matchers.Matcher
import com.github.gumtreediff.matchers.Matchers
import com.github.gumtreediff.tree.Tree

abstract class AbstractTreeComparer {
    abstract val srcTree: Tree
    abstract val dstTree: Tree
    val defaultMatcher: Matcher = Matchers.getInstance().matcher
    val editScriptGenerator: EditScriptGenerator = SimplifiedChawatheScriptGenerator()

    fun compare(): EditScript {
        val mapping = this.defaultMatcher.match(this.srcTree, this.dstTree)
        return this.editScriptGenerator.computeActions(mapping)
    }
}