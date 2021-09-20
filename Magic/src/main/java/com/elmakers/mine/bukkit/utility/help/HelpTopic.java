package com.elmakers.mine.bukkit.utility.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.MacroExpansion;
import com.elmakers.mine.bukkit.utility.Messages;

public class HelpTopic {
    public static final int MIN_WORD_LENGTH = 2;
    public static final int MIN_WHOLE_WORD_LENGTH = 4;

    private final String key;
    private final String title;
    private final String text;
    private final String searchText;
    private final String topicType;
    private final String[] lines;
    protected final Map<String, Integer> words;
    protected final Set<String> titleWords;
    protected final Set<String> tagWords;
    protected final int maxCount;

    public HelpTopic(Messages messages, String key, String text, String tags, String topicType) {
        this.key = key;
        this.topicType = topicType;
        MacroExpansion expansion = messages.expandMacros(text);
        text = expansion.getText();
        text = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(text));
        this.text = text;
        String simpleText = ChatColor.stripColor(ChatUtils.getSimpleMessage(text, true));
        this.searchText = simpleText.toLowerCase();
        this.title = expansion.getTitle();

        // Pre-split simple description lines, remove title if present
        String[] allLines = StringUtils.split(simpleText, "\n");
        if (!title.isEmpty() && allLines.length > 1) {
            lines = Arrays.copyOfRange(allLines, 1, allLines.length);
        } else {
            lines = allLines;
        }

        // Track uses of individual words
        words = new HashMap<>();
        int maxCount = 0;
        titleWords = new HashSet<>();
        tagWords = new HashSet<>();
        List<String> helpTopicWords = new ArrayList<>();
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(searchText)));
        List titleWordList = Arrays.asList(ChatUtils.getWords(title.toLowerCase()));
        titleWords.addAll(titleWordList);
        helpTopicWords.addAll(titleWordList);
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(key)));
        tags = tags + " " + expansion.getTags();
        List tagWordList = Arrays.asList(ChatUtils.getWords(tags.toLowerCase()));
        helpTopicWords.addAll(tagWordList);
        tagWords.addAll(tagWordList);
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(topicType)));
        for (String word : helpTopicWords) {
            word = word.trim();
            if (word.isEmpty()) continue;
            Integer count = words.get(word);
            if (count == null) count = 1;
            else count++;
            words.put(word, count);
            maxCount = Math.max(maxCount, count);
        }
        this.maxCount = maxCount;
    }

    @Nonnull
    public String getTitle() {
        if (title.isEmpty()) {
            String[] pieces = StringUtils.split(key, ".");
            return pieces.length > 1 ? pieces[pieces.length - 1] : key;
        }
        return title;
    }

    @Nonnull
    public String getText() {
        return text;
    }

    @Nonnull
    public HelpTopicMatch match(Help help, Collection<String> keywords) {
        return new HelpTopicMatch(help, this, keywords);
    }

    public boolean isValidWord(String keyword) {
        if (keyword.length() < MIN_WORD_LENGTH) return false;
        if (keyword.length() < MIN_WHOLE_WORD_LENGTH && !words.containsKey(keyword)) return false;
        return true;
    }

    public Map<String, Integer> getWordCounts() {
        return words;
    }

    public String[] getLines() {
        return lines;
    }

    public String getKey() {
        return key;
    }

    public String getTopicType() {
        return topicType;
    }
}
