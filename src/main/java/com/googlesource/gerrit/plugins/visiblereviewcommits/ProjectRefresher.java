package com.googlesource.gerrit.plugins.visiblereviewcommits;

import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.query.change.InternalChangeQuery;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProjectRefresher {
  private static final Logger log =
      LoggerFactory.getLogger(ProjectRefresher.class);

  private final GitRepositoryManager repoManager;
  private final Provider<InternalChangeQuery> queryProvider;
  private final String pluginName;

  final String DEFAULT_NAMESPACE = "refs/heads/review/";
  final String namespace;

  @Inject
  ProjectRefresher(GitRepositoryManager repoManager,
                    Provider<InternalChangeQuery> queryProvider,
                    PluginConfigFactory pluginfConfigFactory,
                    @com.google.gerrit.extensions.annotations.PluginName String pluginName
  ) {
    this.repoManager = repoManager;
    this.queryProvider = queryProvider;
    this.pluginName = pluginName;
    this.namespace = getNamespace(pluginfConfigFactory);
  }

  /**
   * Updates all the visible references in the given project
   *
   * We don't look at the details of the event that triggered us, rather we look
   * every time at the current state of the changes and the patch sets. The overhead should be negligible,
   * but this allows to fix any inconsistencies, including the inevitable ones when the plugin is activated
   * when some changes are already present.
   *
   * @param projectNameKey
   * @throws OrmException
   * @throws IOException
   */
  public void updateRefsInProject(Project.NameKey projectNameKey) throws OrmException, IOException {

    Map<String, Ref> allRefs = getAllRefsInRepo(projectNameKey);

    Map<String, ObjectId> targetState = generateTargetState(projectNameKey, allRefs);

    // First, we check the existing references
    for (Map.Entry<String, Ref> entry : allRefs.entrySet()) {
      String refName = entry.getKey();
      ObjectId currentCommitId = entry.getValue().getObjectId();
      if (refName.startsWith(namespace)) {

        log.debug("Looking at " + refName);

        if (!targetState.containsKey(refName)) {
          // Reference exists but should not. This happens when:
          // - The change has been merged
          // - The change has been abandoned
          // - The patch set is not current anymore, e.g. a new patch set is available
          log.debug("Deleting redundant " + refName);
          deleteRef(projectNameKey, refName);
          continue;
        }

        ObjectId requestedCommitId = targetState.get(refName);
        if (currentCommitId.equals(requestedCommitId)) {
          // Ref is correct already, nothing to do, remove the reference from our to-do list
          targetState.remove(refName);
          continue;
        }

        // If we arrive here, the reference exists but shows to the wrong commit. This should theoretically never happen
        // Log a warning and continue, updateRef() will fix this below
        log.warn(
            String.format(
                "Inconsistency found: %s is currently set to %s, this should never, but will be fixed now",
                refName, currentCommitId
            ));
      }
    }

    // Now we check what's still to do in targetState, typically create references for the branch new patch sets
    for (Map.Entry<String, ObjectId> entry : targetState.entrySet()) {
      String refName = entry.getKey();
      ObjectId requestedCommitId = entry.getValue();
      log.debug("Creating reference " + refName + " -> " + requestedCommitId);
      updateRef(projectNameKey, refName, requestedCommitId);
    }
  }

  private String getNamespace(PluginConfigFactory pluginfConfigFactory) {
    String namespace = pluginfConfigFactory.getFromGerritConfig(pluginName).getString("namespace", DEFAULT_NAMESPACE);
    if ( ! namespace.startsWith("refs/") )
      throw new RuntimeException(pluginName + ": namespace must start with refs/");
    if ( ! namespace.endsWith("/") )
      namespace = namespace + "/";
    return namespace;
  }

  private Map<String, Ref> getAllRefsInRepo(Project.NameKey projectNameKey) throws IOException {
    Map<String, Ref> allRefs = new TreeMap<String, Ref>();

    try (Repository repo = repoManager.openRepository(projectNameKey)) {
      return repo.getAllRefs();
    }
  }

  /**
   * Generate the references we want to have in the end.
   *
   * @param projectNameKey
   * @param allRefs
   * @return a map, the are the reference names, the values the ObjectId's, e.g. the commit ID's
   * @throws OrmException
   */
  private Map<String, ObjectId> generateTargetState(Project.NameKey projectNameKey, Map<String, Ref> allRefs) throws OrmException {
    Map<String, ObjectId> targetState = new TreeMap<String, ObjectId>();
    log.debug("Changes in this repository:");
    InternalChangeQuery internalChangeQuery = queryProvider.get();
    List<ChangeData> changeDataList = internalChangeQuery.byProject(projectNameKey);
    for (ChangeData changeData : changeDataList) {
      log.debug("Change: " + changeData.change().getChangeId());
      Change.Status status = changeData.change().getStatus();
      log.debug("Status: " + status);

      if (status != Change.Status.NEW) {
        // Ignore merged or abandonned changes
        continue;
      }

      PatchSet currentPs = changeData.currentPatchSet();
      ObjectId objectId = allRefs.get(currentPs.getRefName()).getObjectId();
      String generatedRefName = generateRefName(changeData.change(), currentPs);
      targetState.put(generatedRefName, objectId);
    }
    return targetState;
  }

  /**
   * Generate the visible reference for the given patch set
   * @param change
   * @param patchSet
   * @return
   */
  private String generateRefName(Change change, PatchSet patchSet) {
    return String.format("%s%d/%d", namespace, change.getId().get(), patchSet.getId().patchSetId);
  }

  /**
   * Update or create the given reference, exceptions logged but not propagated
   *
   * @param projectNameKey
   * @param refName
   * @param objectId
   */
  private void updateRef(Project.NameKey projectNameKey, String refName, ObjectId objectId) {
    try {
      try (Repository repo = repoManager.openRepository(projectNameKey)) {
        final RefUpdate u = repo.updateRef(refName);
        u.setForceUpdate(true);
        u.setNewObjectId(objectId.copy());
        checkRefUpdateResult(u.update());
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Delete the given reference, exceptions logged but not propagated
   *
   * @param projectNameKey
   * @param refName
   */
  private void deleteRef(Project.NameKey projectNameKey, String refName) {
    try {
      try (Repository repo = repoManager.openRepository(projectNameKey)) {
        final RefUpdate u = repo.updateRef(refName);
        u.setForceUpdate(true);
        checkRefUpdateResult(u.delete());
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  private void checkRefUpdateResult(RefUpdate.Result result) throws IOException {
    switch (result) {
      case FAST_FORWARD:
      case NEW:
      case NO_CHANGE:
      case FORCED:
        // Do nothing
        break;
      default: {
        throw new IOException(result.name());
      }
    }
  }
}
