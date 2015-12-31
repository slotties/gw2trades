package gw2trades.repository.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemAttributes {
    private List<Attribute> attributes;

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public static final class Attribute {
        private String attribute;
        private int modifier;

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }

        public int getModifier() {
            return modifier;
        }

        public void setModifier(int modifier) {
            this.modifier = modifier;
        }
    }
}
