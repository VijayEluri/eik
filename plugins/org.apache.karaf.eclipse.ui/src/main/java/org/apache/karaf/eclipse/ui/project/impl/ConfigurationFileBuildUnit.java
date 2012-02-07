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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.Properties;

import org.apache.karaf.eclipse.core.KarafPlatformModel;
import org.apache.karaf.eclipse.ui.IKarafProject;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public class ConfigurationFileBuildUnit extends AbstractKarafBuildUnit {

    /**
     *
     * @param karafPlatformModel
     * @param karafProject
     * @param projectBuilder
     */
    public ConfigurationFileBuildUnit(final KarafPlatformModel karafPlatformModel, final IKarafProject karafProject, final IncrementalProjectBuilder projectBuilder) {
        super(karafPlatformModel, karafProject, projectBuilder);
    }

    /**
     *
     * @param kind
     * @param args
     * @param monitor
     */
    @Override
    public void build(final int kind, @SuppressWarnings("rawtypes") final Map args, final IProgressMonitor monitor) throws CoreException {

        final File configurationDirectory = getKarafPlatformModel().getConfigurationDirectory().toFile();
        final File configFiles[] = configurationDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".cfg");
            }
        });

        final IFolder runtimeFolder = getKarafProject().getFolder("runtime");
        if (!runtimeFolder.exists()) {
            runtimeFolder.create(true, true, monitor);
        }

        final Properties configProperties = getKarafProject().getRuntimeProperties();

        for (final File f : configFiles) {
            if (f.isDirectory()) {
                continue;
            }

            try {
                final FileInputStream in = new FileInputStream(f);
            } catch (final FileNotFoundException e) {
            }

        }
    }
}
