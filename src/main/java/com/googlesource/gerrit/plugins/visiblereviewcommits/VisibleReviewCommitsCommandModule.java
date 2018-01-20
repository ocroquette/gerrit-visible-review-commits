package com.googlesource.gerrit.plugins.visiblereviewcommits;

import com.google.gerrit.sshd.PluginCommandModule;

class VisibleReviewCommitsCommandModule extends PluginCommandModule {
  @Override
  protected void configureCommands() {
    command(SshCommandRefresh.class);
  }
}

