load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "visible-review-commits",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Implementation-Title: Visible Review Commits plugin",
        "Implementation-Version: 1.1-SNAPSHOT",
        "Implementation-URL: https://github.com/ocroquette/gerrit-visible-review-commits",
        "Gerrit-PluginName: visible-review-commits",
        "Gerrit-Module: com.googlesource.gerrit.plugins.visiblereviewcommits.VisibleReviewCommitsModule",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.visiblereviewcommits.VisibleReviewCommitsCommandModule"
    ],
)
