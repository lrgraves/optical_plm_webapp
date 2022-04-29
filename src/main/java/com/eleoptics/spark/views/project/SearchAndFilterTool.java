package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SearchAndFilterTool extends VerticalLayout {
    Icon searchIcon = new Icon(VaadinIcon.SEARCH);
    Button searchArea = new Button(searchIcon);
    TextField searchBox = new TextField("Search");
    HorizontalLayout searchLayout = new HorizontalLayout();
    HorizontalLayout queryTerms = new HorizontalLayout();
    List<String> queryTermList = new ArrayList<>();
    HorizontalLayout topBar = new HorizontalLayout();

    public void SearchAndFilterTool(){
        // Add the query area, which will list every search and filter 'term'
        add(queryTerms);
        queryTermList.add("");

        // Style the search box area for searching specific words in tags etc
        searchBox.setClearButtonVisible(true);
        searchIcon.setSize("15px");
        searchArea.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        searchArea.setVisible(true);
        searchLayout.add(searchBox, searchArea);
        topBar.setPadding(true);
        topBar.add(searchLayout);

        // Add the search area
        add(topBar);

        setWidthFull();



 }

    private void searchProjects(List<Asset> assetList) {
        //System.out.println("Serach area hit.");
        // Filter table according to typed input

        searchArea.addClickListener(event -> {
            log.info("search button clicked");
            Button phraseButton = new Button(searchBox.getValue(), new Icon(VaadinIcon.CLOSE));
            phraseButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            String term = searchBox.getValue();
            searchBox.setValue("");

            queryTerms.add(phraseButton);
            //add term to query term list
            queryTermList.add(term);

            phraseButton.addClickListener(buttonClickEvent -> {
                queryTerms.remove(phraseButton);
                queryTermList.remove(phraseButton.getText());
                try {
                    List<Asset>filteredList = applyProjectsFilter(assetList, queryTermList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            List<Asset> filteredList = new ArrayList<>();

            try {
                filteredList = applyProjectsFilter(assetList, queryTermList);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        searchArea.addClickShortcut(Key.ENTER);

    }

    private List<Asset> applyProjectsFilter(List<Asset> assetList, List<String> queryTerms) throws IOException {
        List<Asset> updatedList = new ArrayList();
        // This is a dumb method but whaterv
        for (Asset asset : assetList) {
            for (String term : queryTerms) {
                if (asset.assetName.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT)) ||
                        asset.tags.contains(term.toLowerCase(Locale.ROOT))) {
                    updatedList.add(asset);
                }
            }
        }

        List<Asset> finalList = new ArrayList<>();
        Map<Asset, Long> counts =
                updatedList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        for (Asset project : counts.keySet()) {
            if (counts.get(project) == queryTerms.size()) {
                finalList.add(project);
            }
        }

        return finalList;
    }


}
