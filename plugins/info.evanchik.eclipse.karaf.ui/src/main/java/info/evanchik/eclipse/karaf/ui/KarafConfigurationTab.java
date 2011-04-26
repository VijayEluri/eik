/**
 * Copyright (c) 2011 Stephen Evanchik
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Stephen Evanchik - initial implementation
 */
package info.evanchik.eclipse.karaf.ui;

import info.evanchik.eclipse.karaf.core.KarafPlatformModel;
import info.evanchik.eclipse.karaf.core.KarafPlatformModelRegistry;
import info.evanchik.eclipse.karaf.core.configuration.FeaturesSection;
import info.evanchik.eclipse.karaf.core.features.Bundle;
import info.evanchik.eclipse.karaf.core.features.Feature;
import info.evanchik.eclipse.karaf.core.features.FeatureResolverImpl;
import info.evanchik.eclipse.karaf.core.features.Features;
import info.evanchik.eclipse.karaf.core.features.FeaturesRepository;
import info.evanchik.eclipse.karaf.core.features.XmlFeaturesRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

/**
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public class KarafConfigurationTab extends AbstractLaunchConfigurationTab {

    /**
     *
     * @author Stephen Evanchik (evanchsa@gmail.com)
     *
     */
    private final class FeaturesContentProvider implements ITreeContentProvider {

        private List<FeaturesRepository> featuresRepositories;

        @Override
        public void dispose() {
            if (featuresResolverJob != null) {
                featuresResolverJob.cancel();
            }
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement == featuresRepositories) {
                return featuresRepositories.toArray();
            } else if (parentElement instanceof FeaturesRepository) {
                final FeaturesRepository featuresRepository = (FeaturesRepository) parentElement;
                return featuresRepository.getFeatures().getFeatures().toArray();
            } else if (parentElement instanceof Features) {
                final Features features = (Features) parentElement;
                return features.getFeatures().toArray();
            } else if (parentElement instanceof Feature) {
                final Feature feature = (Feature) parentElement;
                return ListUtils.union(feature.getFeatures(), feature.getBundles()).toArray();
            } else {
                return new Object[0];
            }
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            if (inputElement == featuresRepositories && inputElement != null) {
                return featuresRepositories.toArray();
            } else if (inputElement instanceof FeaturesRepository) {
                final FeaturesRepository featuresRepo = (FeaturesRepository) inputElement;
                return new Object[] { featuresRepo.getFeatures() };
            } else if (inputElement instanceof Features) {
                final Features features = (Features) inputElement;
                return features.getFeatures().toArray();
            } else if (inputElement instanceof Feature) {
                final Feature feature = (Feature) inputElement;
                return ListUtils.union(feature.getFeatures(), feature.getBundles()).toArray();
            } else {
                return new Object[0];
            }
        }

        @Override
        public Object getParent(final Object element) {
            if (element instanceof FeaturesRepository) {
                return featuresRepositories;
            } else if (element instanceof Features) {
                final Features features = (Features) element;
                return features.getParent();
            } else if (element instanceof Feature) {
                return ((Feature)element).getParent();
            } else {
                return null;
            }
        }

        @Override
        public boolean hasChildren(final Object element) {
            if (element == featuresRepositories && element != null) {
                return featuresRepositories.size() > 0;
            } else if (element instanceof FeaturesRepository) {
                final FeaturesRepository featuresRepository = (FeaturesRepository) element;
                return featuresRepository.getFeatures().getFeatures().size() > 0;
            } else if (element instanceof Features) {
                final Features features = (Features) element;
                return features.getFeatures().size() > 0;
            } else if (element instanceof Feature) {
                final Feature feature = (Feature) element;
                return feature.getBundles().size() > 0 || feature.getFeatures().size() > 0;
            } else {
                return false;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            if (newInput != null) {
                featuresRepositories = (List<FeaturesRepository>) newInput;
            }
        }
    }

    /**
     *
     * @author Stephen Evanchik (evanchsa@gmail.com)
     *
     */
    private final class FeaturesLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(final Object element, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                if (element instanceof FeaturesRepository) {
                    return KarafUIPluginActivator.getDefault().getImageRegistry().get(KarafUIPluginActivator.LOGO_16X16_IMG);
                } else if (element instanceof Feature) {
                    return KarafUIPluginActivator.getDefault().getImageRegistry().get(KarafUIPluginActivator.FEATURE_OBJ_IBM);
                } else if (element instanceof Bundle) {
                    return KarafUIPluginActivator.getDefault().getImageRegistry().get(KarafUIPluginActivator.BUNDLE_OBJ_IMG);
                } else {
                    return null;
                }

            default:
                return null;
            }
        }

        @Override
        public String getColumnText(final Object element, final int columnIndex) {

            switch (columnIndex) {
            case 0:
                if (element instanceof FeaturesRepository) {
                    final FeaturesRepository featuresRepository = (FeaturesRepository) element;
                    if (featuresRepository.getFeatures().getName() != null) {
                        return featuresRepository.getFeatures().getName();
                    } else {
                        return featuresRepository.getName();
                    }
                } else if (element instanceof Features) {
                    final Features features = (Features) element;
                    if (features.getName() != null) {
                        return features.getName();
                    } else  if (features.getParent() != null) {
                        return features.getParent().getName();
                    } else {
                        return null;
                    }
                } else if (element instanceof Feature) {
                    final Feature feature = (Feature) element;
                    return feature.getName();
                } else if (element instanceof Bundle) {
                    final Bundle bundle = (Bundle) element;
                    final String label;
                    if (bundle.getBundleUrl().startsWith(MVN_URL_PREFIX)) {
                        final String[] bundleComponents = bundle.getBundleUrl().split("/"); //$NON-NLS-1$
                        label = bundleComponents[1];
                    } else {
                        label = element.toString();
                    }

                    return label;
                } else {
                    return element.toString();
                }
            case 1:
                if (element instanceof Feature) {
                    final Feature feature = (Feature) element;
                    return feature.getVersion();
                } else {
                    return null;
                }
            default:
                return null;
            }
        }
    }

    /**
     *
     * @author Stephen Evanchik (evanchsa@gmail.com)
     *
     */
    private final class FeaturesResolverJob extends Job {

        private volatile boolean cancel;

        public FeaturesResolverJob() {
            super("Features Resolver");

            setSystem(true);
        }

        @Override
        protected void canceling() {
            cancel = true;
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            final FeaturesSection features =
                (FeaturesSection) Platform.getAdapterManager().getAdapter(
                        karafPlatformModel,
                        FeaturesSection.class
            );

            features.load();

            final List<FeaturesRepository> featuresRepositories = new ArrayList<FeaturesRepository>(features.getRepositoryList().size());
            for (final String repository : features.getRepositoryList()) {
                try {
                    final InputStream stream = new URL(repository).openConnection().getInputStream();

                    final String repositoryName;
                    if (repository.startsWith(MVN_URL_PREFIX)) {
                        final String[] repositoryComponents = repository.split("/"); //$NON-NLS-1$
                        repositoryName = repositoryComponents[1] + "-" + repositoryComponents[2]; //$NON-NLS-1$
                    } else {
                        repositoryName = repository;
                    }

                    final FeaturesRepository newRepo = new XmlFeaturesRepository(repositoryName, stream);
                    featuresRepositories.add(newRepo);
                } catch (final MalformedURLException e) {
                    // What to do here?
                } catch (final IOException e) {
                    // What to do here?
                }

                if (cancel) {
                    return Status.CANCEL_STATUS;
                }
            }

            if (cancel) {
                return Status.CANCEL_STATUS;
            }

            final FeatureResolverImpl fr = new FeatureResolverImpl(featuresRepositories);

            final List<Object> checkedFeatures = new ArrayList<Object>();
            for (final String s : features.getBootFeatureNames()) {
                final Object[] path = fr.getFeaturePath(s);
                Collections.addAll(checkedFeatures, path);
            }

            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    if (!getControl().isDisposed()) {
                        installedFeatures.setInput(featuresRepositories);
                        installedFeatures.setCheckedElements(checkedFeatures.toArray());
                    }
                };
            });

            if (cancel) {
                return Status.CANCEL_STATUS;
            } else {
                return Status.OK_STATUS;
            }
        }

        @Override
        public boolean shouldRun() {
            return !getControl().isDisposed();
        }
    }

    public static final String ID = "info.evanchik.eclipse.karaf.ui.karafGeneralLaunchConfigurationTab"; //$NON-NLS-1$

    private static final String MVN_URL_PREFIX = "mvn:"; //$NON-NLS-1$

    private Composite control;

    private Job featuresResolverJob;

    private CheckboxTreeViewer installedFeatures;

    private KarafPlatformModel karafPlatformModel;

    private Button localConsole;

    private Button remoteConsole;

    @Override
    public void createControl(final Composite parent) {
        featuresResolverJob = new FeaturesResolverJob();

        control = new Composite(parent, SWT.NONE);

        final GridLayout layout = new GridLayout();
        control.setLayout(layout);

        final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        control.setLayoutData(gd);

        createConsoleBlock(control);
        createFeaturesBlock(control);

        setControl(control);
        Dialog.applyDialogFont(control);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ID);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Image getImage() {
        return KarafUIPluginActivator.getDefault().getImageRegistry().get(KarafUIPluginActivator.LOGO_16X16_IMG);
    }

    @Override
    public String getName() {
        return "Karaf";
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            localConsole.setSelection(
                    configuration.getAttribute(KarafLaunchConfigurationConstants.KARAF_LAUNCH_START_LOCAL_CONSOLE, true));
            remoteConsole.setSelection(
                    configuration.getAttribute(KarafLaunchConfigurationConstants.KARAF_LAUNCH_START_REMOTE_CONSOLE, false));

            karafPlatformModel = KarafPlatformModelRegistry.findActivePlatformModel();
        } catch (final CoreException e) {

        }

        if (featuresResolverJob.getState() == Job.NONE) {
            featuresResolverJob.schedule();
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(KarafLaunchConfigurationConstants.KARAF_LAUNCH_START_LOCAL_CONSOLE, localConsole.getSelection());
        configuration.setAttribute(KarafLaunchConfigurationConstants.KARAF_LAUNCH_START_REMOTE_CONSOLE, remoteConsole.getSelection());
    }

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(KarafLaunchConfigurationConstants.KARAF_LAUNCH_START_LOCAL_CONSOLE, true);
        configuration.setAttribute(KarafLaunchConfigurationConstants.KARAF_LAUNCH_START_REMOTE_CONSOLE, false);
    }

    /**
     * Creates the necessary UI elements that control what kind of console to
     * use (i.e. remote, local or both)
     *
     * @param parent
     */
    private void createConsoleBlock(final Composite parent) {
        final Font font = parent.getFont();
        final Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);

        final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        comp.setLayoutData(gd);

        final Group group = new Group(comp, SWT.NONE);
        group.setFont(font);
        layout = new GridLayout(1, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        group.setText("Console");

        final SelectionListener listener = new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                scheduleUpdateJob();
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                scheduleUpdateJob();
            }
        };

        localConsole = createCheckButton(group, "Local console");
        localConsole.addSelectionListener(listener);

        remoteConsole = createCheckButton(group, "Remote console");
        remoteConsole.addSelectionListener(listener);
    }

    /**
     * Creates the necessary UI controls to manipulate the features system.
     *
     * @param parent
     */
    private void createFeaturesBlock(final Composite parent) {
        final Font font = parent.getFont();
        final Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        comp.setLayoutData(gd);

        final Group group = new Group(comp, SWT.NONE);
        group.setFont(font);
        layout = new GridLayout(1, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        group.setText("Features");

        installedFeatures = new ContainerCheckedTreeViewer(group, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        final Tree tree = installedFeatures.getTree();

        final TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        column1.setText("Feature");
        column1.setWidth(300);

        final TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
        column2.setText("Version");
        column2.setWidth(175);

        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 300;
        tree.setLayoutData(gd);
        tree.setHeaderVisible(true);

        installedFeatures.setContentProvider(new FeaturesContentProvider());
        installedFeatures.setLabelProvider(new FeaturesLabelProvider());
        installedFeatures.setInput(null);
    }
}
