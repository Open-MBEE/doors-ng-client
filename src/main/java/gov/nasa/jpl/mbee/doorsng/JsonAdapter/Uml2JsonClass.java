package gov.nasa.jpl.mbee.doorsng.JsonAdapter;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Uml2JsonClass extends Uml2JsonElement {
    protected String name;
    protected ArrayList<Uml2JsonEntity> baggage = new ArrayList<>();

    public Uml2JsonClass(ElementFactory factory, String id, String name) {
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
                .map(Uml2JsonEntity::getId)
                .collect(Collectors.toList()))
            ;

        return super.getSerialization();
    }

    public List<JSONObject> getClassSerialization() {
        List<JSONObject> list = new ArrayList<>();

        list.add(getSerialization());
        list.addAll(baggage.stream()
            .map(Uml2JsonEntity::getSerialization)
            .collect(Collectors.toList()));

        return list;
    }

    private Uml2JsonAttribute addAttribute(String key, String label, String typeId) {
        String baseKeyId = id+"_"+key;
        String hashedKeyId = DigestUtils.sha256Hex(baseKeyId);
        Uml2JsonAttribute attribute = new Uml2JsonAttribute(factory, hashedKeyId, id, label, typeId);
        attribute.init();
        baggage.add(attribute);
        return attribute;
    }

    public Uml2JsonClass addStringProperty(String key, String label, String value) {
        if(value == null) return addNullProperty(key, label);

        Uml2JsonAttribute attribute = addAttribute(key, label, Uml2JsonAttribute.STRING_TYPE_ID);

        attribute.setDefaultValue(new Uml2JsonLiteral(factory, attribute) {
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

    public Uml2JsonClass addIntegerProperty(String key, String label, int value) {
        Uml2JsonAttribute attribute = addAttribute(key, label, Uml2JsonAttribute.INTEGER_TYPE_ID);

        attribute.setDefaultValue(new Uml2JsonLiteral(factory, attribute) {
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

    public Uml2JsonClass addRealProperty(String key, String label, double value) {
        Uml2JsonAttribute attribute = addAttribute(key, label, Uml2JsonAttribute.REAL_TYPE_ID);

        attribute.setDefaultValue(new Uml2JsonLiteral(factory, attribute) {
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

    public Uml2JsonClass addBooleanProperty(String key, String label, boolean value) {
        Uml2JsonAttribute attribute = addAttribute(key, label, Uml2JsonAttribute.BOOLEAN_TYPE_ID);

        attribute.setDefaultValue(new Uml2JsonLiteral(factory, attribute) {
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

    public Uml2JsonClass addNullProperty(String key, String label) {
        Uml2JsonAttribute attribute = addAttribute(key, label, Uml2JsonAttribute.NULL_TYPE_ID);

        attribute.setDefaultValue(new Uml2JsonLiteral(factory, attribute) {
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

    public Uml2JsonClass addStringArrayProperty(String key, String label, List<String> values) {
        Uml2JsonAttribute attribute = addAttribute(key, label, Uml2JsonAttribute.STRING_TYPE_ID);

        JSONArray valuesArray = new JSONArray();

        Uml2JsonLiteral container = new Uml2JsonLiteral(factory, attribute) {
            @Override
            public String getType() {
                return "Expression";
            }

            @Override
            public void init() {
                super.init();
                serialization
                    .put("symbol", "")
                    .put("operand", valuesArray)
                    .remove("value")
                    ;
            }
        };

        attribute.setDefaultValue(container);

        for(int index=0; index<values.size(); index++) {
            String value = values.get(index);

            Uml2JsonLiteral valueEntity = new Uml2JsonLiteral(factory, container, index+"") {
                @Override
                public String getType() {
                    return "LiteralString";
                }

                @Override
                public void init() {
                    super.init();
                    this
                        .put("value", value)
                        ;
                }
            };

            valueEntity.init();

            valuesArray.put(valueEntity.getSerialization());
        }

        return this;
    }

    public Uml2JsonClass addRelation(String key, String label, String targetId) {
        String associationId = DigestUtils.sha256Hex("association:"+key+":"+(id.compareTo(targetId) < 0? id+"."+targetId: targetId+"."+id));
        Uml2JsonAssociation association = new Uml2JsonAssociation(factory, associationId, label, targetId);
        association.init();
        baggage.add(association);

        Uml2JsonAttribute attribute = addAttribute(key, label, targetId);
        attribute.setAssociationId(associationId);
        return this;
    }

    public Uml2JsonClass addLinks(String key, String label, Link[] links) {
        return addStringArrayProperty(key, label, Arrays.stream(links)
            .map(link -> factory.localResourceUriToElementId(link.getValue()))
            .collect(Collectors.toList()));
    }
}
