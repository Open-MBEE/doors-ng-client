package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import org.json.JSONObject;

public abstract class MmsLiteral extends MmsEntity {
    MmsAttribute attribute;
    public MmsLiteral(ElementFactory factory, MmsAttribute attribute) {
        super(factory, attribute.getId()+"_value", attribute.getId());
        this.attribute = attribute;
    }

    @Override
    public void init() {
        super.init();
        serialization
            .put("typeId", JSONObject.NULL)
            ;
    }

    @Override
    public String getVisibility() {
        return "public";
    }

    @Override
    public String getName() {
        return "";

//        String attributeName = attribute.getName();
//        if(attributeName == null) {
//            return "";
//        }
//        else {
//            return attributeName + "_value";
//        }
    }
}
