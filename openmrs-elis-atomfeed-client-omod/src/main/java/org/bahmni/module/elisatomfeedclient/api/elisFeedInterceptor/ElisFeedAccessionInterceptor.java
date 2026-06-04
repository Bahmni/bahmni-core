package org.bahmni.module.elisatomfeedclient.api.elisFeedInterceptor;

import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;

public interface ElisFeedAccessionInterceptor {
    void run(OpenElisAccession openElisAccession);
}
