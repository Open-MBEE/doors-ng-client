package gov.nasa.jpl.mbee.doorsng.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.ValueType;

@OslcNamespace(OslcConstants.OSLC_CORE_NAMESPACE)
@OslcResourceShape(title = "Folder Resource Shape", describes = {"http://open-services.net/ns/core", "http://jazz.net/ns/rm/navigation, http://jazz.net/xmlns/prod/jazz/calm/1.0"})
public class Folder extends AbstractResource
{

    private Map<QName, Object> extended = new HashMap<QName, Object>();

    private String title;
    private String description;
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

    public String getParent() {
        Object parent = this.getExtendedProperties().get(RmConstants.PROPERTY_PARENT_FOLDER);
        if (parent != null) {
            return parent.toString();
        }
        return null;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public void setDescription(final String description)
    {
        this.description = description;
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

}
