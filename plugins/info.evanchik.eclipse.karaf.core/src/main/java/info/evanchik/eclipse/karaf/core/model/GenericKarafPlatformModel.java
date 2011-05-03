/**
 * Copyright (c) 2009 Stephen Evanchik
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Stephen Evanchik - initial implementation
 */
package info.evanchik.eclipse.karaf.core.model;

import info.evanchik.eclipse.karaf.core.KarafCorePluginUtils;
import info.evanchik.eclipse.karaf.core.internal.KarafCorePluginActivator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

/**
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public class GenericKarafPlatformModel extends AbstractKarafPlatformModel {

    /**
     * The maximum depth to search for JARs in this model
     */
    public static int MAX_SEARCH_DEPTH = 50;

    /**
     * The root of the Karaf platform installation
     */
    private final IPath rootPlatformPath;

    /**
     *
     * @param platformPath
     */
    public GenericKarafPlatformModel(final IPath platformPath) {
        this.rootPlatformPath = platformPath;
    }

    @Override
    public List<String> getBootClasspath() {
        final List<File> jarFiles = new ArrayList<File>();
        KarafCorePluginUtils.getJarFileList(rootPlatformPath.append("lib").toFile(), jarFiles, 0); //$NON-NLS-1$

        final List<String> bootClasspath = new ArrayList<String>();
        for(final File f : jarFiles) {
            bootClasspath.add(f.getAbsolutePath());
        }

        return bootClasspath;
    }

    @Override
    public IPath getConfigurationDirectory() {
        return rootPlatformPath.append("etc"); //$NON-NLS-1$
    }

    @Override
    public IPath getConfigurationFile(final String key) {
        return getConfigurationDirectory().append(key);
    }

    @Override
    public IPath getPluginRootDirectory() {
        return rootPlatformPath.append("system"); //$NON-NLS-1$
    }

    @Override
    public IPath getRootDirectory() {
        return rootPlatformPath;
    }

    /**
     * Directory based Karaf platform model's typically exist outside of an
     * Eclipse platform on a file system and support customization of their
     * configuration.
     *
     * @return false, always
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected List<URL> getPlatformBundles() {

        final List<File> jarFiles = new ArrayList<File>();
        KarafCorePluginUtils.getJarFileList(getPluginRootDirectory().toFile(), jarFiles,
                        MAX_SEARCH_DEPTH);

        return filesToUrls(jarFiles);
    }

    /**
     * Convert a {@link List} of {@link File} to a {@code List} of {@link URL}
     *
     * @param files
     *            the {@code List} of {@code File}S
     * @return a {@code List} of {@code URL}S, one for each {@code File}
     */
    protected final List<URL> filesToUrls(final List<File> files) {
        final List<URL> urls = new ArrayList<URL>();

        for (final File f : files) {
            try {
                final URL u = f.toURI().toURL();
                urls.add(u);
            } catch (final MalformedURLException e) {
                KarafCorePluginActivator.getLogger().error(
                                "Unable to convert file to URL: " + f.getAbsolutePath(), e);
            }
        }

        return urls;
    }
}