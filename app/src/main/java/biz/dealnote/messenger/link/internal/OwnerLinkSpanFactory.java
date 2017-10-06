package biz.dealnote.messenger.link.internal;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class OwnerLinkSpanFactory {

    private static Pattern ownerPattern;
    private static Pattern topicCommentPattern;

    static {
        ownerPattern = Pattern.compile("\\[(id|club)(\\d+)\\|([^]]+)]");
        topicCommentPattern = Pattern.compile("\\[(id|club)(\\d*):bp(-\\d*)_(\\d*)\\|([^]]+)]");
    }

    public static Spannable withSpans(String input, boolean owners, boolean topics, final ActionListener listener) {
        if (isEmpty(input)) {
            return null;
        }

        List<OwnerLink> ownerLinks = owners ? findOwnersLinks(input) : null;
        List<TopicLink> topicLinks = topics ? findTopicLinks(input) : null;

        int count = Utils.safeCountOfMultiple(ownerLinks, topicLinks);

        if(count > 0){
            List<AbsInternalLink> all = new ArrayList<>(count);

            if(nonEmpty(ownerLinks)){
                all.addAll(ownerLinks);
            }

            if(nonEmpty(topicLinks)){
                all.addAll(topicLinks);
            }

            Collections.sort(all, LINK_COMPARATOR);

            Spannable result = Spannable.Factory.getInstance().newSpannable(replace(input, all));
            for (final AbsInternalLink link : all) {
                //TODO Нужно ли удалять spannable перед установкой новых
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if(listener != null){
                            if(link instanceof TopicLink){
                                listener.onTopicLinkClicked((TopicLink) link);
                            }

                            if(link instanceof OwnerLink){
                                listener.onOwnerClick(((OwnerLink) link).ownerId);
                            }
                        }
                    }
                };

                result.setSpan(clickableSpan, link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return result;
        }

        return Spannable.Factory.getInstance().newSpannable(input);
    }

    public interface ActionListener {
        void onTopicLinkClicked(TopicLink link);
        void onOwnerClick(int ownerId);
    }

    private static List<TopicLink> findTopicLinks(String input) {
        Matcher matcher = topicCommentPattern.matcher(input);

        List<TopicLink> links = null;
        while (matcher.find()) {
            if(links == null){
                links = new ArrayList<>(1);
            }

            TopicLink link = new TopicLink();

            boolean club = matcher.group(1).equals("club");
            link.start = matcher.start();
            link.end = matcher.end();
            link.replyToOwner = Integer.parseInt(matcher.group(2)) * (club ? -1 : 1);
            link.topicOwnerId = Integer.parseInt(matcher.group(3));
            link.replyToCommentId = Integer.parseInt(matcher.group(4));
            link.targetLine = matcher.group(5);
            links.add(link);
        }

        return links;
    }

    private static final Comparator<AbsInternalLink> LINK_COMPARATOR = (link1, link2) -> link1.start - link2.start;

    private static List<OwnerLink> findOwnersLinks(String input) {
        List<OwnerLink> links = null;

        Matcher matcher = ownerPattern.matcher(input);
        while (matcher.find()) {
            if (links == null) {
                links = new ArrayList<>(1);
            }

            boolean club = matcher.group(1).equals("club");
            int ownerId = Integer.parseInt(matcher.group(2)) * (club ? -1 : 1);
            String name = matcher.group(3);
            links.add(new OwnerLink(matcher.start(), matcher.end(), ownerId, name));
        }

        return links;
    }

    public static String getTextWithCollapseOwnerLinks(String input) {
        if (isEmpty(input)) {
            return null;
        }

        List<OwnerLink> links = findOwnersLinks(input);
        return replace(input, links);
    }

    private static String replace(String input, List<? extends AbsInternalLink> links) {
        if (Utils.safeIsEmpty(links)) {
            return input;
        }

        StringBuilder result = new StringBuilder(input);
        for (int y = 0; y < links.size(); y++) {
            AbsInternalLink link = links.get(y);
            int origLenght = link.end - link.start;
            int newLenght = link.targetLine.length();
            shiftLinks(links, link, origLenght - newLenght);
            result.replace(link.start, link.end, link.targetLine);
            link.end = link.end - (origLenght - newLenght);
        }

        return result.toString();
    }

    private static void shiftLinks(List<? extends AbsInternalLink> links, AbsInternalLink after, int count) {
        boolean shiftAllowed = false;
        for (AbsInternalLink link : links) {
            if (shiftAllowed) {
                link.start = link.start - count;
                link.end = link.end - count;
            }

            if (link == after) {
                shiftAllowed = true;
            }
        }
    }

    public static String genOwnerLink(int ownerId, String title) {
        return "[" + (ownerId > 0 ? "id" : "club") + Math.abs(ownerId) + "|" + title + "]";
    }
}