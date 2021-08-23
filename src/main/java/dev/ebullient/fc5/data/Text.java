package dev.ebullient.fc5.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dev.ebullient.fc5.Log;
import io.quarkus.qute.TemplateData;

@TemplateData
public class Text {
    public static final Text NONE = new Text(Collections.emptyList());

    final List<String> content;

    public Text(List<String> text) {
        if (text.isEmpty()) {
            this.content = Collections.emptyList();
        } else {
            List<String> collectedText = text.stream()
                    .filter(x -> !x.isBlank())
                    .flatMap(x -> Arrays.asList(x.replaceAll("------\n", "").split("\n")).stream())
                    .map(x -> x.trim())
                    .collect(Collectors.toList());
            content = convertToMarkdown(collectedText);
        }
    }

    private List<String> convertToMarkdown(List<String> textContent) {
        try {
            ListIterator<String> i = textContent.listIterator();

            boolean listMode = false;
            boolean tableMode = false;

            while (i.hasNext()) {
                String line = i.next();

                if (!line.isBlank()) {
                    line = line
                            .replaceAll("•", "-")
                            .replaceAll(" (DC [0-9]+( [A-Za-z]+)?)", " ==$1==")
                            .replaceAll(" ([0-9]+ ?\\([0-9d +]+\\)) ", " `$1` ")
                            .replaceAll(" ([1-9]+d[0-9]+([0-9+ ]+)?) ", " `$1` ")
                            .replaceAll("((Melee|Ranged) Weapon Attack:)", "*$1*")
                            .replaceAll("- ([^:]+?):", "- **$1:**");

                    // Find the short sentence-like headings. They contain few words,
                    // either alone, or followed by a lot more text. If more than one word,
                    // they have at least two capital letters
                    int pos = line.indexOf('.');
                    if (pos > 0) {
                        String sentence = line.substring(0, pos);
                        // These sentence headings do not contain a :
                        // Either they are followed by at least 10 characters (more text), or stand alone.
                        if (!sentence.contains(":") && !sentence.contains("|")
                                && (pos + 10 < line.length() || pos + 1 == line.length())) {
                            String[] words = sentence.split(" ");
                            int capitals = line.substring(0, pos).split("(?=\\p{Lu})").length;
                            if (words.length == 1 || (words.length <= 5 && capitals >= 2)) {
                                if (pos + 1 == line.length() && !line.startsWith("-")) {
                                    line = "## " + line.substring(0, line.length() - 1);
                                } else {
                                    line = line.replaceAll("^(.+?\\.)", "**$1**").replaceAll("^\\*\\*- ", "- **");
                                }
                            }
                        }
                    }
                    i.set(line);
                }

                listMode = handleList(i, line, listMode);
                tableMode = handleTable(i, line, tableMode);

                if (line.startsWith("##") || line.startsWith("Source:")) {
                    insertBlankLineAbove(i);
                }

                if (!listMode && !tableMode && !line.isBlank()) {
                    insertLine(i, "");
                }
            }

            return textContent;
        } catch (Exception e) {
            Log.err().println("Unable to convert entry to markdown: " + e.getMessage());
            Log.err().println("Source text: ");
            Log.err().println(e);
            Log.err().println("Details: ");
            e.printStackTrace(Log.err());
            throw e;
        }
    }

    boolean handleList(ListIterator<String> i, String line, boolean listMode) {
        boolean newListMode = line.startsWith("-");
        if (listMode != newListMode) {
            insertBlankLineAbove(i);
        }
        return newListMode;
    }

    boolean handleTable(ListIterator<String> i, String line, boolean tableMode) {
        boolean newTableMode = line.indexOf('|') >= 0;
        if (newTableMode) {
            if (tableMode != newTableMode) {
                insertBlankLineAbove(i);
            }
            line = "|" + line + "|";
            i.set(line);
            if (tableMode != newTableMode) {
                insertLine(i, String.join("|", line.replaceAll("[^|]", "-")));
            }
        } else if (tableMode && tableMode != newTableMode) {
            insertBlankLineAbove(i);
        }
        return newTableMode;
    }

    void insertLine(ListIterator<String> i, String newLine) {
        i.add(newLine);
        // reset the cursors around the new line for later checks
        i.previous();
        i.next();
    }

    void insertBlankLineAbove(ListIterator<String> i) {
        if (i.previousIndex() > 1) {
            // Move the cursor backwards two
            i.previous();
            String previous = i.previous();
            // use next to put it in the right place to add
            i.next();
            if (!previous.isBlank()) {
                i.add("");
            }
            i.next(); // move cursor back to current line
        }
    }

    public MatchResult matches(Pattern pattern) {
        Matcher matcher = pattern.matcher(String.join("\n", content));
        if (matcher.find()) {
            return matcher.toMatchResult();
        }
        return null;
    }

    public boolean contains(String string) {
        return content.stream().anyMatch(x -> x.contains(string));
    }
}
