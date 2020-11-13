package com.ddkolesnik.avitotraiding.repository;

import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * @author Alexandr Stegnin
 */

public interface Grabber {

    Document getDocument(String url) throws IOException;

    int getTotalPages(String url);

}
