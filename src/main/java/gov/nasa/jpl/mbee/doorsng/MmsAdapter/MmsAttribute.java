package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import org.json.JSONObject;

import java.util.Collections;

public class MmsAttribute extends MmsElement {
    public static final String STRING_TYPE_ID = "_9_0_2_91a0295_1110274713995_297054_0";
    public static final String INTEGER_TYPE_ID = "donce_1051693917650_319078_0";
    public static final String REAL_TYPE_ID = "_17_0beta_f720368_1291217394082_340077_1886";
    public static final String BOOLEAN_TYPE_ID = "_12_0EAPbeta_be00301_1157529792739_987548_11";
    public static final String NULL_TYPE_ID = null;

    protected String typeId;
    protected String name;

    public MmsAttribute(ElementFactory factory, String id, String ownerId, String name, String typeId) {
        super(factory, id, ownerId);
        this.name = name;
        this.typeId = typeId;
    }

    @Override
    public void init() {
        super.init();
        this
            .put("visibility", "private")
            .put("typeId", typeId)
            .put("isOrdered", false)
            .put("isUnique", true)
            .put("lowerValue", JSONObject.NULL)
            .put("upperValue", JSONObject.NULL)
            .put("isReadOnly", false)
            .put("templateParameterId", JSONObject.NULL)
            .put("endIds", Collections.EMPTY_LIST)
            .put("deploymentIds", Collections.EMPTY_LIST)
            .put("aggregation", "none")
            .put("associationEndId", JSONObject.NULL)
            .put("qualifierIds", Collections.EMPTY_LIST)
            .put("datatypeId", JSONObject.NULL)
            .put("defaultValue", JSONObject.NULL)
            .put("interfaceId", JSONObject.NULL)
            .put("isDerived", false)
            .put("isDerivedUnion", false)
            .put("isID", false)
            .put("redefinedPropertyIds", Collections.EMPTY_LIST)
            .put("subsettedPropertyIds", Collections.EMPTY_LIST)
            .put("associationId", JSONObject.NULL)
            ;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "Property";
    }

    @Override
    public String getName() {
        return this.name;
    }

    public MmsAttribute setDefaultValue(MmsLiteral defaultValue) {
        defaultValue.init();
        this.put("defaultValue", defaultValue.getSerialization());
        return this;
    }

    public MmsAttribute setAssociationId(String associationId) {
        this.put("associationId", associationId);
        return this;
    }
}
