package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MmsClass extends MmsElement {
    protected String name;
    protected ArrayList<MmsAttribute> attributes = new ArrayList<>();

    public MmsClass(ElementFactory factory, String id, String name) {
        super(factory, id, factory.getProjectId() + "_pm");
        this.name = name;
    }

    @Override
    public void init() {
        super.init();
        serialization
            .put("elementImportIds", Collections.EMPTY_LIST)
            .put("packageImportIds", Collections.EMPTY_LIST)
            .put("templateBindingIds", Collections.EMPTY_LIST)
            .put("useCaseIds", Collections.EMPTY_LIST)
            .put("representationId", JSONObject.NULL)
            .put("collaborationUseIds", Collections.EMPTY_LIST)
            .put("generalizationIds", Collections.EMPTY_LIST)
            .put("powertypeExtentIds", Collections.EMPTY_LIST)
            .put("redefinedClassifierIds", Collections.EMPTY_LIST)
            .put("substitutionIds", Collections.EMPTY_LIST)
            .put("classifierBehaviorId", JSONObject.NULL)
            .put("interfaceRealizationIds", Collections.EMPTY_LIST)
            .put("ownedOperationIds", Collections.EMPTY_LIST)
            .put("isAbstract", false)
            .put("isActive", false)
            .put("isFinalSpecialization", false)
            ;
    }

    @Override
    public String getType() {
        return "Class";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JSONObject getSerialization() {
        serialization
            .put("ownedAttributeIds", attributes.stream()
                .map(attribute -> attribute.getId())
                .collect(Collectors.toList()))
            ;

        return super.getSerialization();
    }

    public List<JSONObject> getClassSerialization() {
        List<JSONObject> list = new ArrayList<>();

        list.add(getSerialization());
        list.addAll(attributes.stream()
            .map(attribute -> attribute.getSerialization())
            .collect(Collectors.toList()));

        return list;
    }

    public MmsClass addStringProperty(String key, String label, String value) {
        MmsAttribute attribute = new MmsAttribute(factory, id+"_"+key, id, label, MmsAttribute.STRING_TYPE_ID);
        attribute.init();

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralString";
            }

            @Override
            public void init() {
                super.init();
                serialization
                    .put("value", value)
                    ;
            }
        });

        attributes.add(attribute);
        return this;
    }

    public MmsClass addIntegerProperty(String key, String label, int value) {
        MmsAttribute attribute = new MmsAttribute(factory, id+"_"+key, id, label, MmsAttribute.INTEGER_TYPE_ID);
        attribute.init();

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralInteger";
            }

            @Override
            public void init() {
                super.init();
                serialization
                    .put("value", value)
                    ;
            }
        });

        attributes.add(attribute);
        return this;
    }

    public MmsClass addRealProperty(String key, String label, double value) {
        MmsAttribute attribute = new MmsAttribute(factory, id+"_"+key, id, label, MmsAttribute.REAL_TYPE_ID);
        attribute.init();

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralReal";
            }

            @Override
            public void init() {
                super.init();
                serialization
                    .put("value", value)
                    ;
            }
        });

        attributes.add(attribute);
        return this;
    }

    public MmsClass addBooleanProperty(String key, String label, boolean value) {
        MmsAttribute attribute = new MmsAttribute(factory, id+"_"+key, id, label, MmsAttribute.BOOLEAN_TYPE_ID);
        attribute.init();

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralBoolean";
            }

            @Override
            public void init() {
                super.init();
                serialization
                    .put("value", value)
                    ;
            }
        });

        attributes.add(attribute);
        return this;
    }
}
