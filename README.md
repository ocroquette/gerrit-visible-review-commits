# Visible Review Commits plugin for Gerrit

## Introduction


[Gerrit](https://www.gerritcodereview.com/) keeps the references to the review commits (e.g. patch sets) in a special name space ```refs/changes/x/y/z```. It is cumbersome for a normal user to fetch these references.

This plugin duplicates those references in a namespace that can be fetched easily with typical Git clients, e.g.:
```refs/heads/reviews/<change>/<patchset>```

To avoid cluttering of the local repositories, only the active, current patch sets are kept.

## Installation

There is currently no binary distribution of the plugin, only source code, so you will have to build it yourself (see below).

Then, the usual procedure to install Gerrit plugins apply, e.g. use the SSH interface, or copy it in the ```<site>/plugins``` directory, and make sure it is enabled.


## Configuration

Currently, the plugin doesn't support any configuration. It will apply to all projects, and the namespace is hard-coded.

## Activation

Currently, the plugin currently reacts only to reference changes in a given project. So after enabling it, make sure you modify a reference, for instance by creating or deleting a branch, or creating a new review. In a future version, the plugin will do that automatically when it gets enabled.

## Building from source

### Prerequisites

This plugin is built with Bazel from Gerrit's source tree, and [the same prerequisites](https://gerrit-review.googlesource.com/Documentation/dev-bazel.html) as for building Gerrit apply. Currently, it is possible only on Linux and macOS.

### Building

```
git clone https://gerrit.googlesource.com/gerrit
cd gerrit
git checkout v2.15-rc2

cd plugins

git clone  https://github.com/ocroquette/gerrit-visible-review-commits   visible-review-commits

bazel build plugins/gerrit-visible-review-commits
```

The resulting JAR file can be found at:

```
bazel-genfiles/plugins/ref-protection/visible-review-commits.jar
```

## Links

See also the following links:

* [Discussion on the forum](https://groups.google.com/forum/#!topic/repo-discuss/7QgLHhK6Qw0)
* [gerrit-refsfilter](https://github.com/GerritForge/gerrit-refsfilter), another approach


