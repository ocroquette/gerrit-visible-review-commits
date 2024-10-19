package com.googlesource.gerrit.plugins.visiblereviewcommits;
 
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.sshd.PluginCommandModule;
import com.google.inject.Inject;
 
class VisibleReviewCommitsCommandModule extends PluginCommandModule {
  @Inject
  VisibleReviewCommitsCommandModule(@PluginName String pluginName) {
    super(pluginName);
  }
 
  @Override
  protected void configureCommands() {
    command(SshCommandRefresh.class);
  }
}
