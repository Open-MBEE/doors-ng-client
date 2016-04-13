package gov.nasa.jpl.mbee.doorsng.model;

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
public final class RequirementCollection extends Requirement
{
    // The only extra field is uses
    private final Set<URI> uses = new TreeSet<URI>();

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

    public void addUses(final URI uses)
    {
        this.uses.add(uses);
    }

    @OslcDescription("A collection uses a resource - the resource is in the requirement collection.")
    @OslcName("uses")
    @OslcPropertyDefinition(RmConstants.REQUIREMENTS_MANAGEMENT_NAMESPACE + "uses")
    @OslcRange(RmConstants.TYPE_REQUIREMENT)
    @OslcTitle("Uses")
    public URI[] getUses()
    {
        return uses.toArray(new URI[uses.size()]);
    }

    public void setUses(final URI[] uses)
    {
        this.uses.clear();

        if (uses != null)
        {
            this.uses.addAll(Arrays.asList(uses));
        }
    }

    public void clearUses() {
        this.uses.clear();
    }
}
