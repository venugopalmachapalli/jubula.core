/*******************************************************************************
 * Copyright (c) 2014 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.internal.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.jubula.client.ObjectMapping;
import org.eclipse.jubula.client.exceptions.LoadResourceException;
import org.eclipse.jubula.client.internal.utils.SerilizationUtils;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading object mapping associations
 * @author BREDEX GmbH
 * @created Oct 09, 2014
 */
public class ObjectMappingImpl implements ObjectMapping {
    /** the logger */
    private static Logger log = LoggerFactory.getLogger(
            ObjectMappingImpl.class);
    
    /** object mapping associations */
    private Properties m_objectMappingAssociations = new Properties();

    /** object mapping associations */
    private Map<String, ComponentIdentifier> m_map =
            new TreeMap<String, ComponentIdentifier>();
    
    /**
     * Utility class for loading object mapping association
     * @param input the input stream containing the encoded object mapping
     */
    public ObjectMappingImpl(InputStream input) {
        super();
        try {
            m_objectMappingAssociations.load(input);
            for (Object obj : m_objectMappingAssociations.keySet()) {
                if (obj instanceof String) {
                    String compName = (String) obj;
                    if (m_map.containsKey(compName)) {
                        log.error("There is already a mapping for the component name " //$NON-NLS-1$
                                + compName);
                    } else {
                        try {
                            m_map.put(compName, getIdentifier(compName));
                        } catch (LoadResourceException e) {
                            log.error(e.getLocalizedMessage(), e);
                        }                    
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while initialising the ObjectMappingLoader", e); //$NON-NLS-1$
        }
    }
    
    /** {@inheritDoc} */
    public ComponentIdentifier get(String compName) {
        return m_map.get(compName);
    }
    
    /**
     * Returns the component identifier for a component name
     * 
     * @param compName
     *            the component name
     * @return the component identifier or <code>null</code> if no identifier
     *         was found
     * @throws LoadResourceException 
     */
    private ComponentIdentifier getIdentifier(String compName) throws
                LoadResourceException {
        try {
            String encodedString =
                    m_objectMappingAssociations.getProperty(compName);
            if (encodedString != null) {
                Object decodedObject = SerilizationUtils.decode(encodedString);
                if (decodedObject instanceof ComponentIdentifier) {
                    return (ComponentIdentifier) decodedObject;
                }
                throw new LoadResourceException("The decoded object is " //$NON-NLS-1$
                        + "not of type 'IComponentIdentfier'."); //$NON-NLS-1$
            }
        } catch (IOException e) {
            throw new LoadResourceException("Could load the given component name", e); //$NON-NLS-1$
        } catch (ClassNotFoundException e) {
            throw new LoadResourceException("Problems during deserialization...", e); //$NON-NLS-1$
        }
        return null;
    }
}