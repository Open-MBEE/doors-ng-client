package gov.nasa.jpl.mbee.doorsng;
/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *   Gabriel Ruelas    - initial API and implementation
 *   Carlos A Arreola    - initial API and implementation
 *   Samuel Padgett    - avoid unnecessary URISyntaxException
 *******************************************************************************/

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;

@OslcNamespace(RmConstants.REQUIREMENTS_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Requirement Resource Shape", describes = RmConstants.TYPE_REQUIREMENT)
public class Requirement extends org.eclipse.lyo.client.oslc.resources.Requirement
{

    private final Set<URI> rdfTypes = new TreeSet<URI>();
    private final Map<QName, Object> extended = new HashMap<QName, Object>();
    private String resourceUrl;
    private String eTag;

    public Requirement()
    {
        super();

        // Only add the type if Requirement is the created object
        if ( ! ( this instanceof RequirementCollection ) ) {
            rdfTypes.add(URI.create(RmConstants.TYPE_REQUIREMENT));
        }
    }

    public Requirement(final URI about)
    {
        super(about);

        // Only add the type if Requirement is the created object
        if ( ! ( this instanceof RequirementCollection ) ) {
            rdfTypes.add(URI.create(RmConstants.TYPE_REQUIREMENT));
        }
    }

    public String getCustomField(URI property) {
        try {
            return this.getExtendedProperties().get(convertUriToQname(property)).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setCustomField(URI property, Object value) {
        extended.put(convertUriToQname(property), value);
        this.setExtendedProperties(extended);
    }

    public String getPrimaryText() {
        try {
            return this.getExtendedProperties().get(RmConstants.PROPERTY_PRIMARY_TEXT).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setPrimaryText(final String primaryText) {
        extended.put(RmConstants.PROPERTY_PRIMARY_TEXT, primaryText);
        this.setExtendedProperties(extended);
    }

    public String getParent() {
        Object parent = this.getExtendedProperties().get(RmConstants.PROPERTY_PARENT_FOLDER);
        if (parent != null) {
            return parent.toString();
        }
        return null;
    }

    public void setParent(final URI parent) {
        extended.put(RmConstants.PROPERTY_PARENT_FOLDER, parent);
        this.setExtendedProperties(extended);
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(final String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getEtag() {
        return eTag;
    }

    public void setEtag(final String eTag) {
        this.eTag = eTag;
    }

    private static QName convertUriToQname(URI uri) {
        String path = uri.getPath();
        String last = path.substring(path.lastIndexOf('/') + 1);
        String url = uri.toString().replace(last, "");
        return new QName(url, last);
    }

}
