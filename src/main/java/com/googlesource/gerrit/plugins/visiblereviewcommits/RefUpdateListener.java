/*
 *  The MIT License
 *
 *  Copyright 2014 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.googlesource.gerrit.plugins.visiblereviewcommits;

import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.RefUpdatedEvent;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class RefUpdateListener implements EventListener {

  private static final Logger log =
      LoggerFactory.getLogger(RefUpdateListener.class);

  private final ProjectRefresher projectRefresher;

  final String DEFAULT_NAMESPACE = "refs/heads/review/";

  @Inject
  RefUpdateListener(ProjectRefresher projectRefresher) {
    this.projectRefresher = projectRefresher;
  }

  @Override
  public void onEvent(Event event) {
    log.debug("Got event: " + event.toString());

    if (!(event instanceof RefUpdatedEvent)) {
      return;
    }

    RefUpdatedEvent refUpdate = (RefUpdatedEvent) event;

    try {
      projectRefresher.updateRefsInProject(refUpdate.getProjectNameKey());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }
}
