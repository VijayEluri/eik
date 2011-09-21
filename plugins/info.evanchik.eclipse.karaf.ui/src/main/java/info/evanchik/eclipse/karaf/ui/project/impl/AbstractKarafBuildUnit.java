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
package info.evanchik.eclipse.karaf.ui.project.impl;

import info.evanchik.eclipse.karaf.core.KarafPlatformModel;
import info.evanchik.eclipse.karaf.ui.IKarafProject;
import info.evanchik.eclipse.karaf.ui.project.KarafBuildUnit;

/**
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public abstract class AbstractKarafBuildUnit implements KarafBuildUnit {

    private final KarafPlatformModel karafPlatformModel;

    private final IKarafProject karafProject;

    /**
     *
     * @param karafPlatformModel
     * @param karafProject
     */
    public AbstractKarafBuildUnit(final KarafPlatformModel karafPlatformModel, final IKarafProject karafProject) {
        this.karafPlatformModel = karafPlatformModel;
        this.karafProject = karafProject;
    }

    protected final KarafPlatformModel getKarafPlatformModel() {
        return karafPlatformModel;
    }

    protected final IKarafProject getKarafProject() {
        return karafProject;
    }
}