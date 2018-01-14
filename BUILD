load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "visible-review-commits",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Implementation-Title: Visible Review Commits plugin",
        "Implementation-URL: https://github.com/ocroquette/gerrit-visible-review-commits",
        "Gerrit-PluginName: visible-review-commits",
        "Gerrit-Module: com.googlesource.gerrit.plugins.visiblereviewcommits.VisibleReviewCommitsModule",
    ],
)

junit_tests(
    name = "ref_protection_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["ref-protection"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":ref-protection__plugin",
    ],
)
