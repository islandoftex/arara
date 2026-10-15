+++
title = "Install and update"
description = "Local development, pinned installations and theme updates."
weight = 1
updated = 2026-10-06
+++

Use Zola 0.23.6. The installed theme must have this structure:

```text
themes/devlab-theme/
├── theme.toml
├── templates/
├── sass/
└── static/
```

## Local checkout

From the root of this theme repository, create a new site directory and link the theme.
Choose a different directory name if `../my-site` already exists.

```sh
mkdir -p ../my-site/content ../my-site/themes
ln -s "$PWD" ../my-site/themes/devlab-theme
cd ../my-site
```

Create the site's configuration and first page with [Quick start](@/docs/getting-started/quick-start.md).
The symbolic link uses the current checkout, including local changes.

## Repository checkout before a release

The development branch can be used before a version tag is published. For a site
stored in Git, run from the site root:

```sh
git submodule add https://codeberg.org/RiPetitor/devlab-theme themes/devlab-theme
git -C themes/devlab-theme rev-parse HEAD
```

Commit the submodule pointer with the site. It records an exact revision even
though the initial checkout comes from `main`. Review that revision's release
notes and build the site before using it. A repository checkout does not create
a published tag or archive. Run `git init` first if the site has no repository.

## Published release

Choose a published version from the repository's
[tags](https://codeberg.org/RiPetitor/devlab-theme/tags). The commands below
select `v0.9.0` only after its tag is published. Before publication, use the
repository checkout above. The check runs before adding a submodule.

For a site stored in Git, run from the site root:

```sh
(
  DEVLAB_VERSION=v0.9.0
  git ls-remote --exit-code --tags https://codeberg.org/RiPetitor/devlab-theme "refs/tags/$DEVLAB_VERSION" >/dev/null || {
    echo "Tag $DEVLAB_VERSION is not published. Use the repository checkout."
    exit 1
  }
  git submodule add https://codeberg.org/RiPetitor/devlab-theme themes/devlab-theme &&
  git -C themes/devlab-theme fetch --depth 1 origin tag "$DEVLAB_VERSION" &&
  git -C themes/devlab-theme checkout --detach "$DEVLAB_VERSION"
)
```

If the site is not yet a Git repository, run `git init` first. Commit
`.gitmodules` and the `themes/devlab-theme` pointer with the site configuration.
Another checkout restores the recorded theme revision with:

```sh
git submodule update --init --recursive
```

To store ordinary files instead, download the selected tag's source archive
and extract it into `themes/devlab-theme`. `theme.toml` must sit directly in that
directory, not inside another archive folder. Commit the extracted files with
the site and record the selected version in the commit.

## Update

Keep site content, templates and styles outside the installed theme directory.
When upgrading from v0.8, complete the
[configuration migration](@/docs/guides/upgrading-v0-8.md) first.
Read the [release notes](@/blog/devlab-theme-v0-9-0.md) and review local template
overrides before changing its revision.

For a submodule, check for local edits and select the new published tag.
Change `DEVLAB_VERSION` below to the release you are installing:

```sh
git -C themes/devlab-theme status --short
DEVLAB_VERSION=v0.9.0
git -C themes/devlab-theme fetch --depth 1 origin tag "$DEVLAB_VERSION"
git -C themes/devlab-theme checkout --detach "$DEVLAB_VERSION"
zola check --skip-external-links
zola build
```

Move any edits shown by `status` into the site before switching revisions.
Review the affected pages and template overrides, then commit the changed
submodule pointer. `git submodule update --remote` follows a branch rather
than the version selected here.

For a vendored installation, replace the theme directory with the new archive.
Overlaying it would leave files removed by the new release. Run the same checks.

To roll back, restore the previous site commit, including its configuration,
content, overrides and theme revision. For a submodule, run
`git submodule update --init --recursive` after restoring that commit.
