package com.simpleparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class App {
    private static final String URL = "https://habr.com";

    public static void main( String[] args ) throws IOException, URISyntaxException {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Document document = Jsoup.connect(URL + "/ru/all/page" + i).get();
            Elements postTitleElements = document.getElementsByAttribute("data-article-link");
            for (Element element : postTitleElements) {
                try {
                    Post post = parsePostPage(element);
                    posts.add(post);
                    System.out.println(post);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static Post parsePostPage(Element element) throws URISyntaxException, IOException, ParseException {
        String postURL = URL + (element.attr("href"));
        Document postDocument = Jsoup.connect(postURL).get();
        Date date = getDateFromDatetimeAttr(postDocument.getElementsByAttribute("datetime").attr("datetime"));
        List<String> habs = getHabs(postDocument.getElementsByClass("tm-article-snippet__hubs-item-link"));
        return new Post()
                .setUrl(new URI(postURL))
                .setHeader(element.text())
                .setAuthorNickname(Objects.requireNonNull(postDocument
                        .getElementsByClass("tm-user-info__username").first()).text())
                .setDate(date)
                .setRating(Integer.parseInt(postDocument
                        .getElementsByClass("tm-votes-meter__value_rating").text()))
                .setHabs(habs);
    }

    private static Date getDateFromDatetimeAttr(String datetime) throws ParseException {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return inFormat.parse(datetime);
    }

    private static List<String> getHabs(Elements elements) {
        return elements.stream().map(element -> Objects.requireNonNull(element.firstElementChild()).text())
                .collect(Collectors.toList());
    }
}
