package aosp.working.diffextractor

import org.eclipse.jgit.diff.DiffEntry

class ChangeFiles {

    // TODO 是否应该检查 push 的元素是否已经存在于 “某个” Set 中。
    var addFileSet = mutableListOf<String>()
    var delFileSet = mutableSetOf<String>()
    var updateFileSet = mutableSetOf<String>()

    /**
     * 向三个容器中 push 新的文件元素。
     * @param diffEntry 根据 diffEntry 的 ChangeType 来检测该 push 到哪里。
     * @return true 推入成功，false 已经存在 or ChangeType 是 RENAME 或 COPY
     */
    fun push(diffEntry: DiffEntry) = when(diffEntry.changeType) {
        DiffEntry.ChangeType.ADD -> this.addFileSet.add(diffEntry.newPath)
        DiffEntry.ChangeType.MODIFY -> this.updateFileSet.add(diffEntry.oldPath)
        DiffEntry.ChangeType.DELETE -> this.delFileSet.add(diffEntry.oldPath)
        else -> false
    }
}