package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for classType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="classType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hd" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="proficiency" type="{}abilityAndSkillList"/>
 *         &lt;element name="spellAbility" type="{}abilityEnum"/>
 *         &lt;element name="numSkills" type="{}integer"/>
 *         &lt;element name="autolevel" type="{}autolevelType"/>
 *         &lt;element name="armor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="weapons" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tools">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               ...
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="wealth" type="{}rollFormula"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class ClassType implements BaseType {
    public static final String NONE = "none";

    final String name;
    final int hitDice;
    final Proficiency proficiency;
    final AbilityEnum spellAbility;
    final int numSkills;
    final List<Autolevel> autolevel;
    final String armor;
    final String weapons;
    final String tools;
    final String wealth;

    public ClassType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        hitDice = context.getOrDefault("hd", 8);

        proficiency = context.getOrDefault("proficiency", Proficiency.ABILITY_AND_SKILL_LIST);
        proficiency.setFlavor("abilityAndSkillList");

        spellAbility = context.getOrDefault("spellAbility", AbilityEnum.NONE);
        numSkills = context.getOrDefault("numSkills", 0);
        autolevel = context.getOrDefault("autolevel", Collections.emptyList());
        armor = context.getOrDefault("armor", NONE);
        weapons = context.getOrDefault("weapons", NONE);
        tools = context.getOrDefault("tools", NONE);
        wealth = context.getOrDefault("wealth", "");
    }

    public List<Autolevel> getLevelFeatures() {
        return autolevel;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("class/" + MarkdownWriter.slugifier().slugify(name));
    }

    public int getHitDice() {
        return hitDice;
    }

    public int getHitRollAverage() {
        return hitDice / 2 + 1;
    }

    public String getArmor() {
        return armor;
    }

    public String getSavingThrows() {
        return proficiency.getSavingThrows();
    }

    public int getNumSkills() {
        return numSkills;
    }

    public String getSkills() {
        return proficiency.getSkillNames();
    }

    public String getTools() {
        return tools;
    }

    public String getWeapons() {
        return weapons;
    }

    public List<Section> getSortedLevelFeatures() {
        // Collect all of the autolevel features
        List<Section> allSections = autolevel.stream()
                .flatMap(x -> x.features.stream())
                .map(x -> new Section(x))
                .collect(Collectors.toList());

        // Find named groups (to derive sections later), e.g. 
        // Primal Path -> Primal Path: Path of the Berserker -> Path of the Berserker: Frenzy
        List<Section> groups = new ArrayList<>();

        Section classFeatures = new Section("##", "Class Features");
        groups.add(classFeatures);

        List<Section> remaining = new ArrayList<>(allSections);
        for (Section index : allSections) {
            if (!index.grouped) {
                if (index.findFeatureGroups(remaining, "##")) {
                    groups.add(index);
                }
            }
        }

        // Add unclaimed features
        classFeatures.addAll(remaining);

        return groups.stream()
                .flatMap(x -> x.flatten())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ClassType [name=" + name + "]";
    }

    @TemplateData
    class Section {
        final Feature feature;
        String depth;
        String title;
        boolean grouped = false;
        List<Section> children = Collections.emptyList();

        public Section(String depth, String title) {
            this.depth = depth;
            this.title = title;
            this.feature = null;
            this.grouped = true;
        }

        public Section(Feature feature) {
            this.feature = feature;
            this.depth = "";
            this.title = feature.getName();
        }

        void addAll(List<Section> children) {
            this.children = children;
            children.forEach(child -> {
                if (child.grouped) {
                    throw new IllegalStateException("Bad accounting: catchall includes a grouped element");
                }
                child.depth = depth + "#";
            });
            children.sort(leveledAlphabeticalSort);
        }

        boolean findFeatureGroups(List<Section> allSections, String depth) {
            this.depth = depth;
            List<Section> matches = allSections.stream()
                    .filter(x -> x != this)
                    .filter(x -> x.belongsTo(this.title))
                    .peek(x -> x.grouped = true)
                    .collect(Collectors.toList());

            if (!matches.isEmpty()) {
                grouped = true;
                allSections.remove(this);
                allSections.removeIf(x -> matches.contains(x));

                for (Section match : matches) {
                    match.title = match.title.replaceFirst(this.title + "[: ]*", "").trim();
                    match.findFeatureGroups(allSections, depth + "#");
                }
                children = matches;
                return true;
            }
            return false;
        }

        public String getDepth() {
            return depth;
        }

        public String getTitle() {
            return title;
        }

        public String getLevel() {
            return feature == null ? "" : "" + feature.level;
        }

        public String getText() {
            if (feature != null) {
                return feature.getText();
            }
            return "";
        }

        public boolean belongsTo(String prefix) {
            return title.startsWith(prefix) && !title.equals(prefix);
        }

        public Stream<Section> flatten() {
            return Stream.concat(Stream.of(this),
                    children.stream().flatMap(x -> x.flatten()));
        }

        @Override
        public String toString() {
            return String.format("%s %s (grouped=%s, children=%s)",
                    depth, title, grouped, children.size());
        }
    }

    public static Comparator<Section> leveledAlphabeticalSort = new Comparator<>() {
        @Override
        public int compare(Section o1, Section o2) {
            if (o1.feature.level == o2.feature.level) {
                return o2.title.compareTo(o2.title);
            }
            return o1.feature.level - o2.feature.level;
        }
    };
}
