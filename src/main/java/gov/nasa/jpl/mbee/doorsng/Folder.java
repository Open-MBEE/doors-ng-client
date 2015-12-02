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
 *	 Gabriel Ruelas	   - initial API and implementation
 *	 Carlos A Arreola	 - initial API and implementation
 *	 Samuel Padgett	   - avoid unnecessary URISyntaxException
 *******************************************************************************/

import java.net.URI;

import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.ValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.client.oslc.resources.RmConstants;

@OslcNamespace(OslcConstants.OSLC_CORE_NAMESPACE)
@OslcResourceShape(title = "Requirement Resource Shape", describes = RmConstants.JAZZ_RM_NAV_NAMESPACE + "parent")
public class Folder extends AbstractResource
{
    private String title;
	private String description;
    private String parent;
    private String resourceUrl;

    public Folder()
    {
        super();
    }

    public Folder(final URI about)
    {
        super(about);
    }


	@OslcDescription("Title (reference: Dublin Core) or often a single line summary of the resource represented as rich text in XHTML content.")
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "title")
	@OslcTitle("Title")
	@OslcValueType(ValueType.XMLLiteral)
	public String getTitle()
	{
		return title;
	}

	@OslcDescription("Descriptive text (reference: Dublin Core) about resource represented as rich text in XHTML content.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "description")
	@OslcTitle("Description")
	@OslcValueType(ValueType.XMLLiteral)
	public String getDescription()
	{
		return description;
	}

    public String getParent()
    {
        return parent;
    }

	public void setTitle(final String title)
	{
		this.title = title;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

    public void setParent(final String parent)
    {
        this.parent = parent;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(final String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }
}
