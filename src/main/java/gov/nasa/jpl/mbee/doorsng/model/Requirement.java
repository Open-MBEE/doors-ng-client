package gov.nasa.jpl.mbee.doorsng.model;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.MmsAdapter.ElementFactory;
import gov.nasa.jpl.mbee.doorsng.MmsAdapter.MmsClass;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.oslc4j.core.annotation.*;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.eclipse.lyo.oslc4j.core.model.ValueType;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@OslcNamespace(RmConstants.REQUIREMENTS_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Requirement Resource Shape", describes = RmConstants.TYPE_REQUIREMENT)
public class Requirement extends org.eclipse.lyo.client.oslc.resources.Requirement
{

    private final Set<URI> rdfTypes = new TreeSet<URI>();
    private Map<QName, Object> extended = new HashMap<QName, Object>();
    private String resourceUrl;
    private String eTag;

    private String primaryText;

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

    public Object getCustomFieldObject(URI property) {
        try {
            return this.getExtendedProperties().get(convertUriToQname(property));
        } catch (Exception e) {
            return null;
        }
    }
    public void setCustomField(URI property, Object value) {
        extended.put(convertUriToQname(property), value);
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

    @OslcDescription("Primary Text field.")
    @OslcPropertyDefinition("http://jazz.net/ns/rm#primaryText")
    @OslcTitle("Primary Text")
    @OslcValueType(ValueType.String)
    public String getPrimaryText() {
        return this.primaryText;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public List<JSONObject> export(DoorsClient doors, ElementFactory factory) throws URISyntaxException, RuntimeException {
        MmsClass requirement = factory.createClass(DigestUtils.sha256Hex(getAbout().toString()), getTitle());

        // basic properties
        requirement
            .addStringProperty("identifier", "Identifier", getIdentifier())
            .addStringProperty("primaryText", "Primary Text", getPrimaryText())
            .addStringProperty("about", "About", getAbout().toString())
            .addStringProperty("created", "Date Created", getCreated().toString())
            .addStringProperty("modified", "Date Last Modified", getModified().toString())
//            .addStringProperty("creators", "Creators", Arrays.stream(getCreators()).map().toString())
            ;

//        for(Link validator: getValidatedBy()) {
//            requirement.addRelation("validatedBy", "Validated By", validator.getValue().toString());
//        }
//
//        for(Link satisfier: getSatisfiedBy()) {
//            requirement.addRelation("satisfiedBy", "Satisfied By", satisfier.getValue().toString());
//        }
//
//        for(Link satisfied: getSatisfies()) {
//            requirement.addRelation("satisfies", "Satisfies", validator.getValue().toString());
//        }


        ResourceShape resourceShape = doors.getResource(ResourceShape.class, this.getInstanceShape());

        // each extended property
        Map<QName, Object> properties = this.getExtendedProperties();
        for(Map.Entry<QName, Object> property: properties.entrySet()) {
            // uri of the property
            QName qname = property.getKey();
            URI propertyUri = new URI(qname.getNamespaceURI()+qname.getLocalPart());

            // create deterministic id of propertyUri for it's JSON key
            String propertyId = propertyUri.toString();

            // fetch property's label
            Property propertyResource = resourceShape.getProperty(propertyUri);
            String propertyLabel;
            if(propertyResource == null) {
                propertyLabel = qname.getPrefix()+":"+qname.getLocalPart();
            }
            else {
                propertyLabel = propertyResource.getTitle();
            }

            // ref value
            Object valueObject = property.getValue();

            // verbose debugging
//            System.err.println("["+propertyUri.toString()+" ==> "+valueObject.toString()+"]");
            System.err.println(String.format("~~ (%s) \"%s\" <%s>", valueObject == null? "null": valueObject.getClass().getName(), propertyLabel, propertyUri));

            if(valueObject instanceof String) {
                requirement.addStringProperty(propertyId, propertyLabel, (String) valueObject);
            }
            else if(valueObject instanceof URI) {
                String targetId = factory.createClass(((URI) valueObject).toString(), "").getId();
//                requirement.addRelation(propertyId, propertyLabel, targetId);
            }
            else if(valueObject instanceof Integer) {
                requirement.addIntegerProperty(propertyId, valueObject.toString(), (Integer) valueObject);
            }
            else if(valueObject instanceof Double) {
                requirement.addRealProperty(propertyId, propertyLabel, (Double) valueObject);
            }
            else if(valueObject instanceof Date) {
                requirement.addStringProperty(propertyId, propertyLabel, ((Date) valueObject).toString());
            }
            else if(valueObject == null) {
                throw new RuntimeException(String.format("Encountered null value type for property \"%s\" <%s>", propertyLabel, propertyUri));
            }
            else {
                throw new RuntimeException(String.format("Value type of custom property \"%s\" (%s) does not match known datatypes", propertyLabel, valueObject.getClass().getName()));
            }

//            Object resource = doors.getResource(String.class, new URI(qname.toString()));
//            Object field = this.getCustomFieldObject(propertyUri);

        }

//        //
//         for (Property property : properties) {
//             if (property.getTitle() != null) {
//                 String value = current.getCustomField(property.getPropertyDefinition());
//                 res.put(property.getTitle(), value != null ? value : "");
//             }
//         }

        return requirement.getClassSerialization();
    }
}
