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
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.lyo.client.oslc.resources.RmConstants;

@OslcNamespace(RmConstants.REQUIREMENTS_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Requirement Resource Shape", describes = RmConstants.TYPE_REQUIREMENT)
public class Requirement extends org.eclipse.lyo.client.oslc.resources.Requirement {
    
    private final Set<URI>    rdfTypes                  = new TreeSet<URI>();
    private String            sysmlid;
    
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

    @OslcDescription("Sysmlid to map back to mms.")
    @OslcOccurs(Occurs.ExactlyOne)
    @OslcPropertyDefinition("https://doors-ng-uat.jpl.nasa.gov:9443/rm/types/__TU3sYHCEeWxYp5ZPr3Qqg")
    @OslcTitle("sysmlid")
    @OslcValueType(ValueType.String)
    @OslcName("__TU3sYHCEeWxYp5ZPr3Qqg") // name has to match end of property
    // definition
    public String getSysmlId() {
        return sysmlid;
    }

    public void setSysmlId(final String sysmlid) {
        this.sysmlid = sysmlid;
    }

}
