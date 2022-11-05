package com.simpleparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

public class SimpleParser {

    private static final String BASIC_URL = "https://habr.com";
    private static final Logger logger = LoggerFactory.getLogger(SimpleParser.class.getName());

    public static void main( String[] args ) throws IOException, URISyntaxException {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String url = BASIC_URL + "/ru/all/page" + i;
            logger.info("Connection with url... [url = {}]", url);
            Document document = Jsoup.connect(BASIC_URL + "/ru/all/page" + i).get();
            Elements postTitleElements = document.getElementsByAttribute("data-article-link");
            for (Element element : postTitleElements) {
                try {
                    Post post = parsePostPage(element);
                    logger.info("Post was parsed [post = {}]", post);
                    posts.add(post);
                } catch (NullPointerException e) {
                    logger.error("Extracting data error [message = {}]", e.getMessage());
                } catch (ParseException e) {
                    logger.error("Parsing date error [message = {}]", e.getMessage());
                }
            }
        }

        writeToFile(posts);
    }

    private static Post parsePostPage(Element element) throws URISyntaxException, IOException, ParseException {
        String postURL = BASIC_URL + (element.attr("href"));
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

    private static void writeToFile(List<Post> posts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            for (Post post : posts) {
                writer.write(post.toString() + "\n");
            }
        } catch (IOException e) {
            logger.error("Writing to file error [message = {}]", e.getMessage());
        }
    }
}
