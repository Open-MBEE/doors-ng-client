package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import org.json.JSONObject;

public abstract class MmsLiteral extends MmsEntity {
    MmsEntity owner;
    public MmsLiteral(ElementFactory factory, MmsEntity owner) {
        super(factory, owner.getId()+"_value", owner.getId());
        this.owner = owner;
    }

    public MmsLiteral(ElementFactory factory, MmsEntity owner, String idMod) {
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
