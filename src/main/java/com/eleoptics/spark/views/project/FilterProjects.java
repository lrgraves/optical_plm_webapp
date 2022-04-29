package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.eventbus.EventBusFactory;
import com.eleoptics.spark.events.*;
import com.eleoptics.spark.views.main.MainView;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.css.grid.GridLayoutComponent;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.MinMax;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@CssImport(value = "./styles/style.css", include = "lumo-badge")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class FilterProjects extends VerticalLayout {

    VerticalLayout searchAndFilterLayout = new VerticalLayout();
    Icon searchIcon = new Icon(VaadinIcon.SEARCH);
    Button searchArea = new Button(searchIcon);
    TextField searchBox = new TextField("Search");
    HorizontalLayout searchLayout = new HorizontalLayout();
    HorizontalLayout queryTerms = new HorizontalLayout();
    List<String> queryTermList = new ArrayList<>();
    HorizontalLayout topBar = new HorizontalLayout();
    Boolean projectLevel = false;

    // Filtering of Optical Performance
    BigDecimal minFocalLength = BigDecimal.valueOf(-Float.MAX_VALUE);
    BigDecimal maxFocalLength = BigDecimal.valueOf(Float.MAX_VALUE);
    BigDecimal minFNumber = BigDecimal.valueOf(-Float.MAX_VALUE);
    BigDecimal maxFNumber = BigDecimal.valueOf(Float.MAX_VALUE);
    BigDecimal minWavelength = BigDecimal.valueOf(0.0);
    BigDecimal maxWavelength = BigDecimal.valueOf(Float.MAX_VALUE);
    BigDecimal minEntrancePupilDiameter = BigDecimal.valueOf(0.0);
    BigDecimal maxEntrancePupilDiameter = BigDecimal.valueOf(Float.MAX_VALUE);
    Integer minSurfaces = 0;
    Integer maxSurfaces = Integer.MAX_VALUE;
    List<String> materialList = new ArrayList<>();

    BigDecimalField minFocalLengthField = new BigDecimalField("min");
    BigDecimalField maxFocalLengthField = new BigDecimalField("max");
    BigDecimalField minFNumberField = new BigDecimalField("min");
    BigDecimalField maxFNumberField = new BigDecimalField("max");
    BigDecimalField minWavelengthField = new BigDecimalField("min");
    BigDecimalField maxWavelengthField = new BigDecimalField("max");
    BigDecimalField minEntrancePupilDiameterField = new BigDecimalField("min");
    BigDecimalField maxEntrancePupilDiameterField = new BigDecimalField("max");
    IntegerField minSurfacesField = new IntegerField("min");
    IntegerField maxSurfacesField = new IntegerField("max");
    TextField materialListBox = new TextField("");


    Button filterButton = new Button("Filter");
    Button clearFilterButton = new Button("Reset");


    // Asset list for the project
    List<Asset> filteredProjectsList = new ArrayList<>();
    List<Asset> originalFullProjectsList = new ArrayList<>();
    List<Asset> allAssets = new ArrayList<>();


    private Optional<Consumer<List<Asset>>> filterHandler = Optional.empty();

    public void filterHandler(Consumer<List<Asset>> filterListener){
        filterHandler = Optional.of(filterListener);
    }


    public FilterProjects() {
        // Initialize the search and filter area and get a vertical layout
        constructSearchAndFilterLayout();
        //searchAndFilterLayout.getStyle().set("margin-left", "auto");
        searchAndFilterLayout.setWidth("70%");

        searchArea.addClickListener(event -> searchProjectEvent());
        searchArea.addClickShortcut(KeyModifier.ENTER);
        add(searchAndFilterLayout);

    }

    class Spectrum {
        public Float minWavlength;
        public Float maxWavelength;

        public Spectrum(Float minimumWavelength, Float maximumWavelength) {
            minWavlength= minimumWavelength;
            maxWavelength = maximumWavelength;
        }
    }



    // Call this to externally define the assets to be filtered
    public void defineProjectLists(List<Asset> originalList, List<Asset> allAssetList) {
        originalFullProjectsList = originalList;
        allAssets = allAssetList;
        filteredProjectsList = originalList;
    }

    public void defineProjectLevel(Boolean isProjectLevel) {
        projectLevel = isProjectLevel;
    }

    private void constructSearchAndFilterLayout() {
        // Initialize the search box for a new term
        searchBox.setClearButtonVisible(true);
        queryTermList.add("");
        searchIcon.setSize("15px");
        searchArea.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        searchArea.setVisible(true);
        searchLayout.add(searchBox, searchArea);

        // Create a topbar that combines the search box and the search icon
        topBar.setPadding(true);
        topBar.getStyle().set("padding-bottom", "0px");
        topBar.getStyle().set("margin-bottom", "0px");
        topBar.add(searchLayout);

        // Style the text big decimal field


        // Focal Length Layout
        VerticalLayout focalLengthFilter = new VerticalLayout();
        Span focalLengthSpan = new Span("Focal Length [mm]");
        focalLengthSpan.getStyle().set("font-size", "16px");

        focalLengthFilter.add(focalLengthSpan, new HorizontalLayout(minFocalLengthField, maxFocalLengthField));
        focalLengthFilter.setAlignItems(Alignment.START);
        focalLengthFilter.getStyle().set("padding", "0px");
        focalLengthFilter.getStyle().set("margin-top", "0px");
        focalLengthFilter.getStyle().set("margin-bottom", "0px");

        // F Number Layout
        VerticalLayout fNumberFilter = new VerticalLayout();
        Span fNumberSpan = new Span("F-Number");
        fNumberSpan.getStyle().set("font-size", "16px");

        fNumberFilter.add(fNumberSpan, new HorizontalLayout(minFNumberField, maxFNumberField));
        fNumberFilter.setAlignItems(Alignment.START);
        fNumberFilter.getStyle().set("padding", "0px");
        fNumberFilter.getStyle().set("margin-top", "8px");
        fNumberFilter.getStyle().set("margin-bottom", "0px");

        // Wavelength Layout
        VerticalLayout wavelengthFilter = new VerticalLayout();
        Span wavelengthSpan = new Span("Spectrum [um]");
        wavelengthSpan.getStyle().set("font-size", "16px");

        wavelengthFilter.add(wavelengthSpan, new HorizontalLayout(minWavelengthField, maxWavelengthField));
        wavelengthFilter.setAlignItems(Alignment.START);
        wavelengthFilter.getStyle().set("padding", "0px");
        wavelengthFilter.getStyle().set("margin-top", "8px");
        wavelengthFilter.getStyle().set("margin-bottom", "0px");

        // Entrance Pupil Diameter Layout
        VerticalLayout entrancePupilDiameterFilter = new VerticalLayout();
        Span entrancePupilDiameterSpan = new Span("Entrance Pupil Diam. [mm]");
        entrancePupilDiameterSpan.getStyle().set("font-size", "16px");

        entrancePupilDiameterFilter.add(entrancePupilDiameterSpan, new HorizontalLayout(minEntrancePupilDiameterField,
                maxEntrancePupilDiameterField));
        entrancePupilDiameterFilter.setAlignItems(Alignment.START);
        entrancePupilDiameterFilter.getStyle().set("padding", "0px");
        entrancePupilDiameterFilter.getStyle().set("margin-top", "8px");
        entrancePupilDiameterFilter.getStyle().set("margin-bottom", "0px");

        // Surfaces Layout
        VerticalLayout surfacesFilter = new VerticalLayout();
        Span surfacesSpan = new Span("Surfaces in System");
        surfacesSpan.getStyle().set("font-size", "16px");

        surfacesFilter.add(surfacesSpan, new HorizontalLayout(minSurfacesField, maxSurfacesField));
        surfacesFilter.setAlignItems(Alignment.START);
        surfacesFilter.getStyle().set("padding", "0px");
        surfacesFilter.getStyle().set("margin-top", "8px");
        surfacesFilter.getStyle().set("margin-bottom", "0px");

        // Material Select
        VerticalLayout materialFilter = new VerticalLayout();
        Span materialSpan = new Span("Materials");
        materialSpan.getStyle().set("font-size", "16px");
        materialListBox.setPlaceholder("n-bk7");
        materialFilter.add(materialSpan, materialListBox);
        materialFilter.setAlignItems(Alignment.START);
        materialFilter.getStyle().set("padding", "0px");
        materialFilter.getStyle().set("margin-top", "8px");
        materialFilter.getStyle().set("margin-bottom", "0px");

        // Style Filter button
        filterButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        clearFilterButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

        // Construct the Filter stuff
        VerticalLayout filterLayout = new VerticalLayout();
        filterLayout.add(focalLengthFilter, fNumberFilter, wavelengthFilter, entrancePupilDiameterFilter,
                materialFilter, new HorizontalLayout(filterButton, clearFilterButton));

        filterLayout.setAlignItems(Alignment.START);

        // Setup the listener
        filterButton.addClickListener(event -> {
            // Focal Length
            // Min focal length
            if (!minFocalLengthField.isEmpty()) {
                minFocalLength = minFocalLengthField.getValue();
            } else {
                minFocalLength = BigDecimal.valueOf(-Float.MAX_VALUE);
            }

            // Max focal length
            if (!maxFocalLengthField.isEmpty()) {
                maxFocalLength = maxFocalLengthField.getValue();
            } else {
                maxFocalLength = BigDecimal.valueOf(Float.MAX_VALUE);
            }

            // F-Number
            // Min f-number length
            if (!minFNumberField.isEmpty()) {
                minFNumber = minFNumberField.getValue();
            } else {
                minFNumber = BigDecimal.valueOf(-Float.MAX_VALUE);
            }

            // Max f-number length
            if (!maxFNumberField.isEmpty()) {
                maxFNumber = maxFNumberField.getValue();
            } else {
                maxFNumber = BigDecimal.valueOf(Float.MAX_VALUE);
            }

            // Wavelength
            // Min wavelength
            if (!minWavelengthField.isEmpty()) {
                minWavelength = minWavelengthField.getValue();
            } else {
                minWavelength = BigDecimal.valueOf(0.0);
            }

            // Max wavelength
            if (!maxWavelengthField.isEmpty()) {
                maxWavelength = maxWavelengthField.getValue();
            } else {
                maxWavelength = BigDecimal.valueOf(Float.MAX_VALUE);
            }

            // Entrance Pupil Diameter
            // Min EPD
            if (!minEntrancePupilDiameterField.isEmpty()) {
                minEntrancePupilDiameter = minEntrancePupilDiameterField.getValue();
            } else {
                minEntrancePupilDiameter = BigDecimal.valueOf(0.0);
            }

            // Max EPD
            if (!maxEntrancePupilDiameterField.isEmpty()) {
                maxEntrancePupilDiameter = maxEntrancePupilDiameterField.getValue();
            } else {
                maxEntrancePupilDiameter = BigDecimal.valueOf(Float.MAX_VALUE);
            }

            // Surfaces
            // Min surfaces
            if (!minSurfacesField.isEmpty()) {
                minSurfaces = minSurfacesField.getValue();
            } else {
                minSurfaces = 0;
            }

            // Max surfaces
            if (!maxSurfacesField.isEmpty()) {
                maxSurfaces = maxSurfacesField.getValue();
            } else {
                maxSurfaces = Integer.MAX_VALUE;
            }

            // Materials
            if (!materialListBox.isEmpty()) {
                String materialString = materialListBox.getValue().toLowerCase(Locale.ROOT).replaceAll("\\s", "");
                String[] materialArray = materialString.split("[,]");
                log.info("material split string " + materialArray.toString());
                materialList = new ArrayList<>(Arrays.asList(materialArray));
            } else {
                materialList = new ArrayList<>();
            }

            filterAssetTool();

        });
        clearFilterButton.addClickListener(event -> {
            minFocalLengthField.clear();
            maxFocalLengthField.clear();
            minFNumberField.clear();
            maxFNumberField.clear();
            minWavelengthField.clear();
            maxWavelengthField.clear();
            minEntrancePupilDiameterField.clear();
            maxEntrancePupilDiameterField.clear();
            materialListBox.clear();
            minSurfacesField.clear();
            maxSurfacesField.clear();
            filterButton.click();

            if(!filterHandler.isEmpty()){
                filterHandler.get().accept(originalFullProjectsList);
            }
        });
        searchAndFilterLayout.add(topBar, queryTerms, filterLayout);
        searchAndFilterLayout.setAlignItems(Alignment.START);

    }

    private void searchProjectEvent() {
        {
            //log.info("search button clicked");
            Button phraseButton = new Button(searchBox.getValue(), new Icon(VaadinIcon.CLOSE));
            phraseButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            // Get value and reset serach box area
            String term = searchBox.getValue();
            searchBox.setValue("");

            queryTerms.add(phraseButton);
            //add term to query term list
            queryTermList.add(term);

            // Handle if the phrase added is removed
            phraseButton.addClickListener(buttonClickEvent -> {
                queryTerms.remove(phraseButton);
                queryTermList.remove(phraseButton.getText());
                try {
                    List<Asset> filteredList = applyProjectsSearch(filteredProjectsList, queryTermList);
                    // Use this new filtered list to populate your grid.
                    if(!filterHandler.isEmpty()){
                        filterHandler.get().accept(filteredList);
                    }
                   // EventBusFactory.getEventBus().post(new ProjectListSearchedEvent(filteredList));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

            List<Asset> filteredList = new ArrayList<>();


            // Apply new filtering
            try {
                log.info("search engaged");
                filteredList = applyProjectsSearch(filteredProjectsList, queryTermList);

                if(!filterHandler.isEmpty()){
                    filterHandler.get().accept(filteredList);
                }
                // Use this new filtered list to populate your grid!
               // EventBusFactory.getEventBus().post(new ProjectListSearchedEvent(filteredList));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void filterAssetTool() {
        // this should adjust the overall designpathlist
        // search through all assets to determine  which has a focal length greater than min focal length

        log.info("All asset list is " + allAssets.toString());
        // Create fresh list
        List<Asset> filterList = new ArrayList<>();
        for (int pos = 0; pos < allAssets.size(); pos++) {
            //get current asset
            Asset currentAsset = allAssets.get(pos);
            Metadata assetMap = currentAsset.getMetadata();

            // check if the current asset focal length exceeds minimum filter length
            if (assetMap.opticalMetadata.isPresent()) {

                // Fetch the hashmaps
                OpticalMetadata.Performance performanceData = assetMap.opticalMetadata.get().performance;
                OpticalMetadata.Prescription prescriptionData = assetMap.opticalMetadata.get().prescription;;

                // Get the materials in the project
                List<String> materialStringList = prescriptionData.materials;


                String materialString = materialStringList.toString().toLowerCase(Locale.ROOT);
                materialStringList = Arrays.asList(materialString.split("[\\s, :, \\[, \\] ] "));
                //log.info("The material string list is "+ materialStringList.toString());
                Set<String> materialStringSet = new HashSet<String>(materialStringList);

                //log.info("Word set is " + materialList.toString() + "the length is " + materialList.size());

                // Fetch the other parameters, including focal length, f number, spectral range, EPD
                Float effFocalLength = performanceData.effectiveFocalLength;
                Float fNumber = performanceData.fNumber;
                Float entrancePupilDiameter = performanceData.entrancePupilDiameter;
                Spectrum spectral_range = new Spectrum(performanceData.minimumWavelength, performanceData.maximumWavelength);

                // material bool
                Boolean materialMatch = true;
                //log.info("material match is at stage 1: "+ materialMatch.toString());

                // Filter for focal length and more
                if (effFocalLength >= minFocalLength.floatValue() && effFocalLength <= maxFocalLength.floatValue()
                        && fNumber >= minFNumber.floatValue() && fNumber <= maxFNumber.floatValue()
                        && spectral_range.minWavlength >= minWavelength.floatValue() && spectral_range.maxWavelength <= maxWavelength.floatValue()
                        && entrancePupilDiameter >= minEntrancePupilDiameter.floatValue() && entrancePupilDiameter <= maxEntrancePupilDiameter.floatValue()
                ) {
                    // only check for material if the others are true
                    for (int str = 0; str < materialList.size(); str++) {
                        if (!materialStringSet.contains(materialList.get(str))) {
                            materialMatch = false;
                            log.info("Material failed to match at " + materialList.get(str));
                        }
                    }
                    // If the parameters match, verify the match is for a design path valid in this case
                    if (materialMatch && !projectLevel) {
                        // add the matching design path to the asset list
                        for (int designPathPos = 0; designPathPos < originalFullProjectsList.size(); designPathPos++) {
                            //log.info("The current asset key is " + originalFullProjectsList.get(designPathPos).key);
                            if (originalFullProjectsList.get(designPathPos).key.equals(currentAsset.parentKey)
                                    && !filterList.contains(originalFullProjectsList.get(designPathPos))) {
                                //log.info("The key matches the child parent key as " + currentAsset.parentKey);
                                filterList.add(originalFullProjectsList.get(designPathPos));
                            }
                        }
                    } else {
                        // we are at the project level, and we just need to report what project matches, dont care about design paths
                        for (Asset designPath : allAssets) {
                            if (designPath.getKey() != null && designPath.getKey().equals(currentAsset.parentKey)) {
                                // we have the design path, just need to find the parent project and add
                                for (Asset project : originalFullProjectsList) {
                                    if (project.getKey() != null && project.getKey().equals(designPath.parentKey) &&
                                            !filterList.contains(project)) {
                                        // if the project level key matches the correct design path parent key, and the
                                        // project isnt in the list, add it.
                                        filterList.add(project);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // set the overall design path list so that the search option works correctly.

        //Verify the array content
        filteredProjectsList = filterList;

        if(!filterHandler.isEmpty()){
            filterHandler.get().accept(filterList);
        }
        //filterList = applyProjectsFilter(filteredProjectsList, queryTermList);
       // EventBusFactory.getEventBus().post(new ProjectListSearchedEvent(filterList));
        //log.info("Filtered list" + filterList.toString());

    }


    private List<Asset> applyProjectsSearch(List<Asset> assetList, List<String> queryTerms) throws IOException {
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



