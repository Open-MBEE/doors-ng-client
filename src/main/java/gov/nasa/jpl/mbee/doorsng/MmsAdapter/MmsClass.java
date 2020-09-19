package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MmsClass extends MmsElement {
    protected String name;
    protected ArrayList<MmsEntity> baggage = new ArrayList<>();

    public MmsClass(ElementFactory factory, String id, String name) {
        super(factory, id, factory.getProjectId() + "_pm");
        this.name = name;
    }

    @Override
    public void init() {
        super.init();
        this
            .put("elementImportIds", Collections.EMPTY_LIST)
            .put("packageImportIds", Collections.EMPTY_LIST)
            .put("templateBindingIds", Collections.EMPTY_LIST)
            .put("useCaseIds", Collections.EMPTY_LIST)
            .put("representationId", JSONObject.NULL)
            .put("collaborationUseIds", Collections.EMPTY_LIST)
            .put("generalizationIds", Collections.EMPTY_LIST)
            .put("powertypeExtentIds", Collections.EMPTY_LIST)
            .put("isAbstract", false)
            .put("isFinalSpecialization", false)
            .put("redefinedClassifierIds", Collections.EMPTY_LIST)
            .put("substitutionIds", Collections.EMPTY_LIST)
            .put("classifierBehaviorId", JSONObject.NULL)
            .put("interfaceRealizationIds", Collections.EMPTY_LIST)
            .put("ownedOperationIds", Collections.EMPTY_LIST)
            .put("isActive", false)
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
        this
            .put("ownedAttributeIds", baggage.stream()
                .map(entity -> entity.getId())
                .collect(Collectors.toList()))
            ;

        return super.getSerialization();
    }

    public List<JSONObject> getClassSerialization() {
        List<JSONObject> list = new ArrayList<>();

        list.add(getSerialization());
        list.addAll(baggage.stream()
            .map(entity -> entity.getSerialization())
            .collect(Collectors.toList()));

        return list;
    }

    private MmsAttribute addAttribute(String key, String label, String typeId) {
        String keyId = DigestUtils.sha256Hex(id+"_"+key);
        MmsAttribute attribute = new MmsAttribute(factory, keyId, id, label, typeId);
        attribute.init();
        baggage.add(attribute);
        return attribute;
    }

    public MmsClass addStringProperty(String key, String label, String value) {
        MmsAttribute attribute = addAttribute(key, label, MmsAttribute.STRING_TYPE_ID);

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralString";
            }

            @Override
            public void init() {
                super.init();
                this.put("value", value);
            }
        });

        return this;
    }

    public MmsClass addIntegerProperty(String key, String label, int value) {
        MmsAttribute attribute = addAttribute(key, label, MmsAttribute.INTEGER_TYPE_ID);

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralInteger";
            }

            @Override
            public void init() {
                super.init();
                this.put("value", value);
            }
        });

        return this;
    }

    public MmsClass addRealProperty(String key, String label, double value) {
        MmsAttribute attribute = addAttribute(key, label, MmsAttribute.REAL_TYPE_ID);

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralReal";
            }

            @Override
            public void init() {
                super.init();
                this.put("value", value);
            }
        });

        return this;
    }

    public MmsClass addBooleanProperty(String key, String label, boolean value) {
        MmsAttribute attribute = addAttribute(key, label, MmsAttribute.BOOLEAN_TYPE_ID);

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralBoolean";
            }

            @Override
            public void init() {
                super.init();
                this.put("value", value);
            }
        });

        return this;
    }

    public MmsClass addNullProperty(String key, String label) {
        MmsAttribute attribute = addAttribute(key, label, MmsAttribute.NULL_TYPE_ID);

        attribute.setDefaultValue(new MmsLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "LiteralNull";
            }

            @Override
            public void init() {
                super.init();
                serialization
                    .remove("value")
                    ;
            }
        });

        return this;
    }

    public MmsClass addRelation(String key, String label, String targetId) {
        String associationId = DigestUtils.sha256Hex("association:"+key+":"+(id.compareTo(targetId) < 0? id+"."+targetId: targetId+"."+id));
        MmsAssociation association = new MmsAssociation(factory, associationId, label, targetId);
        association.init();
        baggage.add(association);

        MmsAttribute attribute = addAttribute(key, label, targetId);
        attribute.setAssociationId(associationId);
        return this;
    }
}
