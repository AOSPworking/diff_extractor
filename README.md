# diff-extractor

为了获得 `ninja-hacked` 以及后续 `cg-generator` 的输入，需要知道两个版本间哪些文件得到了修改，还要知道哪些方法被修改。

因此需要一个工具来从 `git history` 中获取差异。

## 1. Output

输出 `commit` 间具备差异的文件名，并写明那些 “差异文件” 下的 “差异方法”。详情可见 `output.json`。
