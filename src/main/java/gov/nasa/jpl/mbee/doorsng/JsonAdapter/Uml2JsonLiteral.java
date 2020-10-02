package gov.nasa.jpl.mbee.doorsng.JsonAdapter;

import org.json.JSONObject;

public abstract class Uml2JsonLiteral extends Uml2JsonEntity {
    Uml2JsonEntity owner;
    public Uml2JsonLiteral(ElementFactory factory, Uml2JsonEntity owner) {
        super(factory, owner.getId()+"_value", owner.getId());
        this.owner = owner;
    }

    public Uml2JsonLiteral(ElementFactory factory, Uml2JsonEntity owner, String idMod) {
        super(factory, owner.getId()+"_value_"+idMod, owner.getId());
        this.owner = owner;
    }

    @Override
    public void init() {
        super.init();
        this
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
