package dev.ebullient.fc5.pojo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Section {
    public static Comparator<Section> leveledAlphabeticalSort = new Comparator<>() {
        @Override
        public int compare(Section o1, Section o2) {
            if (o1.level == o2.level) {
                return o2.title.compareTo(o2.title);
            }
            return o1.level - o2.level;
        }
    };

    final MdFeature feature;
    final int level;

    String depth;
    String title;
    boolean grouped;
    List<Section> children = Collections.emptyList();

    public Section(String depth, String title) {
        this.depth = depth;
        this.title = title;
        this.feature = null;
        this.grouped = true;
        this.level = 0;
    }

    public Section(MdFeature feature) {
        this.feature = feature;
        this.depth = "";
        this.title = feature.getName();
        this.level = feature.getLevel();
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void addAll(List<Section> children) {
        this.children = children;
        children.forEach(child -> {
            if (child.grouped) {
                throw new IllegalStateException("Bad accounting: catchall includes a grouped element");
            }
            child.depth = depth + "#";
        });
        children.sort(leveledAlphabeticalSort);
    }

    public boolean findFeatureGroups(List<Section> allSections, String depth) {
        this.depth = depth;
        List<Section> matches = allSections.stream()
                .filter(x -> x != this && !x.grouped)
                .filter(x -> !x.title.toLowerCase().contains(" feature"))
                .filter(x -> !x.title.toLowerCase().contains(" improvement"))
                .filter(x -> x.belongsTo(this.title))
                .peek(x -> x.grouped = true) // indicate matching section is part of a group
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
        return level == 0 ? "" : "" + level;
    }

    public String getText() {
        if (feature != null) {
            return feature.getText().replaceAll("\n## ", "\n" + depth + "# ");
        }
        return "";
    }

    public boolean belongsTo(String prefix) {
        return title.startsWith(prefix) && !title.equals(prefix) && title.charAt(prefix.length()) != '-';
    }

    public Stream<Section> flatten() {
        return Stream.concat(Stream.of(this),
                children.stream().flatMap(x -> x.flatten()));
    }

    @Override
    public String toString() {
        return String.format("%s %s (grouped=%s, children=%s, level=%s)",
                depth, title, grouped, children.size(), level);
    }

}
