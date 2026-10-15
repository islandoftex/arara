+++
title = "Code blocks"
description = "Fences, filenames, line numbers and Copy."
weight = 2
updated = 2026-10-06
+++

Markdown fenced code blocks are styled automatically. Their toolbar identifies the language and includes a Copy button when the Clipboard API is available. The button shows a visible success or failure state while a polite live region announces the same result to assistive technology.

The toolbar prints a readable label for the detected language, so `bash` reads `Shell` and `text` reads `Text` instead of Zola's internal syntax name.

````md
```toml
theme = "devlab-theme"
compile_sass = true
```
````

```toml
theme = "devlab-theme"
compile_sass = true
```

Add `name=` to give a block a filename or path caption. DevLab renders it as the block title and shows the language beside it:

````md
```bash,name=build.sh,linenos
make -j"$(nproc)"
```
````

```bash,name=build.sh,linenos
make -j"$(nproc)"
```

The value travels in Zola's native `data-name` attribute, so it is never inserted into the copied text. Use a single path-like token without spaces, because Zola parses the fence info string as a comma-separated attribute list.

Add `linenos` for line numbers and `hl_lines` for individual lines or ranges:

````md
```rust,linenos,hl_lines=2
fn main() {
    println!("this line is highlighted");
    println!("this one is not");
}
```
````

```rust,linenos,hl_lines=2
fn main() {
    println!("this line is highlighted");
    println!("this one is not");
}
```

These annotations are native Zola highlighting features. DevLab styles them and strips line numbers when copying.
