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
package org.apache.karaf.eclipse.workbench;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.karaf.eclipse.workbench.jmx.JMXServiceDescriptor;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author Stephen Evanchik (evanchsa@gmail.com)
 *
 */
public interface MBeanProvider extends IAdaptable {

    /**
     * A string constant used as a key to distinguish multiple implementations
     * of Karaf workbench services in the OSGi service registry
     */
    public static final String KARAF_WORKBENCH_SERVICES_ID =
        "org.apache.karaf.eclipse.jmx.workbench.services";

    /**
     * Retrieves the {@link JMXServiceDescriptor} for this {@code MBeanProvider}
     *
     * @return the {@code JMXServiceDescriptor} for this {@code MBeanProvider}
     */
    public JMXServiceDescriptor getJMXServiceDescriptor();

    /**
     * Retrieves an MBean proxy of the given interface class
     *
     * @param <T>
     *            allows the compiler to know that if the {@code interfaceClass}
     *            parameter is {@code MyMBean.class}, for example, then the
     *            return type is {@code MyMBean}.
     * @param objectName
     *            the name of the MBean to forward to on the remote end point
     * @param interfaceClass
     * @return the new proxy instance
     */
    public <T> T getMBean(ObjectName objectName, Class<T> interfaceClass);

    /**
     * Getter for the {@link MBeanServerConnection}
     *
     * @return the {@link MBeanServerConnection}
     */
    public MBeanServerConnection getMBeanServerConnection();

    /**
     * Determines if this {@code MBeanProvider} has been opened.
     *
     * @return true if this {@code MBeanProvider} is open, false otherwise
     */
    public boolean isOpen();
}
