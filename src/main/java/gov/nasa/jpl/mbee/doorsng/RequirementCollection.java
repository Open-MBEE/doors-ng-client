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
 *     Gabriel Ruelas       - initial API and implementation
 *     Carlos A Arreola     - initial API and implementation
 *     Samuel Padgett       - avoid unnecessary URISyntaxException
 *******************************************************************************/

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRange;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.client.oslc.resources.RmConstants;

@OslcNamespace(RmConstants.REQUIREMENTS_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Requirement Collection Resource Shape", describes = RmConstants.TYPE_REQUIREMENT_COLLECTION)
public final class RequirementCollection extends Requirement {
    // The only extra field is uses
    public RequirementCollection()
    {
        super();

        addRdfType(URI.create(RmConstants.TYPE_REQUIREMENT_COLLECTION));
    }

    public RequirementCollection(final URI about)
    {
        super(about);

        addRdfType(URI.create(RmConstants.TYPE_REQUIREMENT_COLLECTION));
    }
}
