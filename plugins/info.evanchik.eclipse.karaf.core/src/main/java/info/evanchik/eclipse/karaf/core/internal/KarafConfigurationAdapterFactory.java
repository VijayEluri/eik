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
package info.evanchik.eclipse.karaf.core.internal;

import info.evanchik.eclipse.karaf.core.IKarafConstants;
import info.evanchik.eclipse.karaf.core.KarafCorePluginUtils;
import info.evanchik.eclipse.karaf.core.KarafPlatformModel;
import info.evanchik.eclipse.karaf.core.configuration.ConfigurationSection;
import info.evanchik.eclipse.karaf.core.configuration.DelegatingStartupSectionImpl;
import info.evanchik.eclipse.karaf.core.configuration.FeaturesSection;
import info.evanchik.eclipse.karaf.core.configuration.GeneralSection;
import info.evanchik.eclipse.karaf.core.configuration.ManagementSection;
import info.evanchik.eclipse.karaf.core.configuration.StartupSection;
import info.evanchik.eclipse.karaf.core.configuration.SystemSection;
import info.evanchik.eclipse.karaf.core.configuration.internal.FeaturesSectionImpl;
import info.evanchik.eclipse.karaf.core.configuration.internal.GeneralSectionImpl;
import info.evanchik.eclipse.karaf.core.configuration.internal.ManagementSectionImpl;
import info.evanchik.eclipse.karaf.core.configuration.internal.StartupSectionImpl;
import info.evanchik.eclipse.karaf.core.configuration.internal.SystemSectionImpl;
import info.evanchik.eclipse.karaf.core.model.GenericKarafPlatformModel;
import info.evanchik.eclipse.karaf.core.model.WorkingKarafPlatformModel;

import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Implementation of {@link IAdapterFactory} for the default implementations of
 * {@link ConfigurationSection} that the core EIK plugin understands.<br>
 * <br>
 * It is expected that there will be other {@code IAdapterFactory}
 * implementations to facilitate accessing non-standard configuration sections
 * based on a {@link KarafPlatformModel}
 *
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public final class KarafConfigurationAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] ADAPTABLE_TYPES = {
        FeaturesSection.class,
        GeneralSection.class,
        ManagementSection.class,
        StartupSection.class,
        SystemSection.class
    };

    @Override
    public Object getAdapter(final Object adaptableObject, @SuppressWarnings("rawtypes") final Class adapterType) {
        if (adaptableObject instanceof KarafPlatformModel == false) {
            return null;
        }

        final KarafPlatformModel karafModel = (KarafPlatformModel) adaptableObject;

        final Object adaptedObject;
        if (adapterType == FeaturesSection.class) {
            adaptedObject = new FeaturesSectionImpl(karafModel);
        } else if (adapterType == GeneralSection.class) {
            adaptedObject = new GeneralSectionImpl(karafModel);
        } else if (adapterType == ManagementSection.class) {
            adaptedObject = adaptManagementSection(karafModel);
        } else if (adapterType == StartupSection.class) {
            adaptedObject = adaptStartupSection(karafModel);
        } else if (adapterType == SystemSection.class) {
            return new SystemSectionImpl(karafModel);
        } else {
            adaptedObject = null;
        }

        return adaptedObject;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return ADAPTABLE_TYPES;
    }

    /**
     * @param karafModel
     * @return
     */
    private Object adaptManagementSection(final KarafPlatformModel karafModel) {
        final Object adaptedObject;
        if(KarafCorePluginUtils.isServiceMix(karafModel)) {
            adaptedObject = new ManagementSectionImpl(karafModel, IKarafConstants.ORG_APACHE_SERVICEMIX_MANAGEMENT_CFG_FILENAME);
        } else if (KarafCorePluginUtils.isFelixKaraf(karafModel)) {
            adaptedObject = new ManagementSectionImpl(karafModel, IKarafConstants.ORG_APACHE_FELIX_KARAF_MANAGEMENT_CFG_FILENAME);
        } else if (KarafCorePluginUtils.isKaraf(karafModel)) {
            adaptedObject = new ManagementSectionImpl(karafModel, IKarafConstants.ORG_APACHE_KARAF_MANAGEMENT_CFG_FILENAME);
        } else {
            adaptedObject = null;
        }

        return adaptedObject;
    }

    /**
     * @param karafModel
     * @return
     */
    private Object adaptStartupSection(final KarafPlatformModel karafModel) {
        final Object adaptedObject;
        if (karafModel instanceof WorkingKarafPlatformModel) {

            /*
             * This delegates to the original model because that is where
             * the plugins that are used to start the Karaf Platform live.
             */
            final WorkingKarafPlatformModel workingModel =
                (WorkingKarafPlatformModel) karafModel;

            final KarafPlatformModel parentModel = workingModel.getParentKarafModel();
            if (parentModel instanceof GenericKarafPlatformModel) {
                adaptedObject =
                    new DelegatingStartupSectionImpl(
                        parentModel,
                        new StartupSectionImpl(parentModel));
            } else {
                adaptedObject = null;
            }
        } else if (karafModel instanceof GenericKarafPlatformModel) {
            adaptedObject = new StartupSectionImpl(karafModel);
        } else {
            adaptedObject = null;
        }

        return adaptedObject;
    }
}