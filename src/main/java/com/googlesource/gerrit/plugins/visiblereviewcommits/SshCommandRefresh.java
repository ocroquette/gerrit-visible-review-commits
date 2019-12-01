package com.googlesource.gerrit.plugins.visiblereviewcommits;


import com.google.gerrit.entities.Project;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.sshd.SshCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.inject.Inject;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@CommandMetaData(name="refresh", description="Refresh the visible references based on the current changes")
class SshCommandRefresh extends SshCommand {

  @Option(name = "--all", usage = "refresh all projects")
  private boolean all;

  @Option(name = "--project", metaVar = "PROJECTNAME", usage = "refresh the given project only")
  private String projectName;

  ProjectCache projectCache;

  ProjectRefresher projectRefresher;

  private static final Logger log =
      LoggerFactory.getLogger(SshCommandRefresh.class);

  @Inject
  SshCommandRefresh(ProjectCache projectCache, ProjectRefresher projectRefresher) {
    this.projectCache = projectCache;
    this.projectRefresher = projectRefresher;
    log.warn("projectRefresher=" + projectRefresher);
  }

  @Override
  protected void run() {
    if ( all ) {
      for (Project.NameKey key : projectCache.all()) {
        refresh(key);
      }
    }
    else if ( projectName != null && ! projectName.isEmpty() ) {
      for (Project.NameKey key : projectCache.all()) {
        if ( key.toString().equals(projectName)) {
          refresh(key);
        }
      }
    }
    else {
      stderr.println("ERROR: please provide either --all or --project");
    }
  }

  protected void refresh(Project.NameKey projectKey) {
    stdout.println("Refreshing project \"" + projectKey + "\"");
    try {
      projectRefresher.updateRefsInProject(projectKey);
    } catch (IOException e) {
      stderr.println("ERROR: while updating " + projectKey + ": " + e.toString());
    }
  }
}

