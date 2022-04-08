package aosp.working.diffextractor

import aosp.working.diffextractor.utils.JGitUtil

fun main(args: Array<String>) {
    assert(args.size > 3) {
        "The 1st Argument is path of repository\n" +
        "The 2nd Argument is one commit id\n" +
        "The 3rd Argument is another commit id\n"
    }

    val repoPath: String = args[0]
    val firstCommitId: String = args[1]
    val secondCommitId: String = args[2]
}