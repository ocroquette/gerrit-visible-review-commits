# Visible Review Commits plugin for Gerrit

## Introduction


[Gerrit](https://www.gerritcodereview.com/) keeps all references to the review commits (e.g. patch sets) in a special, single namespace, e.g. ```refs/changes/x/y/z```. It is cumbersome for a normal user to fetch these references.

This plugin mirrors the the active patch sets in a namespace that can be fetched easily with typical Git clients, for instance:
```refs/heads/review/<change #>/<patchset #>```. 

The default namespace is in `refs/heads/`, so that the Git clients see and fetch the references automatically. If you want the active path sets to be seen only when fetched explicitely, for instance for a CI server, but not for normal users, you can configure namespace like `refs/ci/` instead.

## Installation

There is currently no binary distribution of the plugin, only source code, so you will have to build it yourself (see below).

Then, the usual procedure to install Gerrit plugins applies, e.g. use the SSH interface, or copy it in the ```<site>/plugins``` directory, and make sure it is enabled.


## Configuration

The default namespace for the review branches is ```refs/heads/review/```.
You can modify this in the Gerrit configuration file, for instance:

```
[plugin "visible-review-commits"]    
    namespace = refs/heads/changes/
```

The plugin will not delete the previous namespace if you modify it in the configuration, so if possible, set the proper value right from the beginning.

## Activation

After the plugin has been activated, it will create and delete the visible references according to the changes status every time another reference is modified in the project, for instance when changes are patched, abandoned or merged. To trigger the initial creation of the visible references, use the SSH command "refresh", as described below. 

## SSH commands

The plugin supports a command called `refresh` to trigger the initial creation of the references or to update them at any later time.

```
$ ssh gerrit visible-review-commits refresh -h

visible-review-commits refresh [--] [--all] [--help (-h)] [--project VAL]

 --            : end of options
 --all         : Refresh all projects
 --help (-h)   : display this help text
 --project VAL : Refresh the given project only
```

## Building from source

### Prerequisites

This plugin is built with Bazel from Gerrit's source tree, and [the same prerequisites](https://gerrit-review.googlesource.com/Documentation/dev-bazel.html) as for building Gerrit apply. Currently, it is possible only on Linux and macOS.

### Building

```
git clone https://gerrit.googlesource.com/gerrit
cd gerrit
git checkout v2.15-rc2

git  clone  https://github.com/ocroquette/gerrit-visible-review-commits  plugins/visible-review-commits

bazel  build  plugins/gerrit-visible-review-commits
```

The resulting JAR file can be found at:

```
bazel-genfiles/plugins/ref-protection/visible-review-commits.jar
```

## Links

See also the following links:

* [Discussion on the forum](https://groups.google.com/forum/#!topic/repo-discuss/7QgLHhK6Qw0)
* [gerrit-refsfilter](https://github.com/GerritForge/gerrit-refsfilter), another approach


