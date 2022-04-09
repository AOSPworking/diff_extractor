package aosp.working.diffextractor.tree

import com.github.gumtreediff.tree.Tree

class ModifyEntryTreeComparer(
    override val srcTree: Tree,
    override val dstTree: Tree): AbstractTreeComparer() {
}