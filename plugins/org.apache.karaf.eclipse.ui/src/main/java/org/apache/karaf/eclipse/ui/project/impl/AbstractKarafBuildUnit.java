/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.karaf.eclipse.ui.project.impl;

import org.apache.karaf.eclipse.core.KarafPlatformModel;
import org.apache.karaf.eclipse.ui.IKarafProject;
import org.apache.karaf.eclipse.ui.project.KarafBuildUnit;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public abstract class AbstractKarafBuildUnit implements KarafBuildUnit {

    private final KarafPlatformModel karafPlatformModel;

    private final IKarafProject karafProject;

    private final IncrementalProjectBuilder incrementalProjectBuilder;

    /**
     *
     * @param karafPlatformModel
     * @param karafProject
     * @param projectBuilder
     */
    public AbstractKarafBuildUnit(final KarafPlatformModel karafPlatformModel, final IKarafProject karafProject, final IncrementalProjectBuilder projectBuilder) {
        this.karafPlatformModel = karafPlatformModel;
        this.karafProject = karafProject;
        this.incrementalProjectBuilder = projectBuilder;
    }

    protected final KarafPlatformModel getKarafPlatformModel() {
        return karafPlatformModel;
    }

    protected final IKarafProject getKarafProject() {
        return karafProject;
    }

    protected final IncrementalProjectBuilder getProjectBuilder() {
        return incrementalProjectBuilder;
    }
}
