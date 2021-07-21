package com.ddkolesnik.avitotraiding.repository;

import com.ddkolesnik.avitotraiding.utils.City;
import com.ddkolesnik.avitotraiding.utils.Company;
import org.jsoup.nodes.Document;

/**
 * @author Alexandr Stegnin
 */

public interface Grabber {

    Document getDocument(String url);

    int getTotalPages(String url);

    int parse(Company company, City city);

    boolean exists(String url);
}
