package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for featureType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="featureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="special" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modifier" type="{}modifierType"/>
 *         &lt;element name="proficiency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="optional" type="{}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@TemplateData
public class Feature {

    final String name;
    final Text text;
    final List<String> special;
    final List<Modifier> modifier;
    final Proficiency proficiency;
    final boolean isOptional;

    public Feature(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        this.text = context.getOrDefault("text", Text.NONE);
        this.special = context.getOrDefault("special", Collections.emptyList());
        this.modifier = context.getOrDefault("modifier", Collections.emptyList());
        this.proficiency = context.getOrDefault("proficiency", Proficiency.STRING);
        proficiency.setFlavor(Proficiency.STRING.flavor);
        this.isOptional = context.getOrDefault("optional", false);
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    @Override
    public String toString() {
        return "FeatureType [name=" + name + "]";
    }
}
