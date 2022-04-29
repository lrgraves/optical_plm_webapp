package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class RenderSVG {

    Integer padding = 0;

    public class SurfacePoints {
        @JsonProperty("x")
        double xValue;
        @JsonProperty("y")
        double yValue;

        public double getxValue() {
            return xValue;
        }

        public double getyValue() {
            return yValue;
        }

        @Override
        public String toString() {
            return "SurfacePoints{" +
                    "xPoints=" + xValue +
                    ", yPoints=" + yValue +
                    ')';
        }

        public SurfacePoints(double x, double y) {
            this.xValue = x;
            this.yValue = y;
        }
    }


    public String createDoc(Asset asset, Integer height, Integer width, Integer paddingValue, VisualizationData dataPoints, Boolean drawRays) throws IOException {

        padding = paddingValue;

        StringBuilder svgString = new StringBuilder();

        // Get the width and diameter to define the proper SVG scaling and size

        Double systemLength = 0.0;
        Double systemDiameter = 0.0;
        Double negX = 0.0;
        Double negY = 0.0;

        //log.info("max length is " + maxLength + " max diameter is " + maxDiameter);
        // Go through every element except the detector
        for (int elementPos = 0; elementPos < dataPoints.system.elements.size(); elementPos++) {
            VisualizationData.InterfaceList element = dataPoints.system.elements.get(elementPos);
            for (int interfacePos = 0; interfacePos < element.interfaces.size(); interfacePos++) {
                VisualizationData.PointsList interfacePoints = element.interfaces.get(interfacePos);
                for (int pointPos = 0; pointPos < interfacePoints.points.length; pointPos++) {
                    Double xPoint = interfacePoints.points[pointPos][0];
                    Double yPoint = interfacePoints.points[pointPos][1] * -1;
                    if (Math.abs(xPoint) > systemLength) {
                        systemLength = Math.abs(xPoint);
                    }
                    if (Math.abs(yPoint) > systemDiameter) {
                        systemDiameter = Math.abs(yPoint);
                    }
                    if (xPoint < negX) {
                        negX = xPoint;

                    }
                    if (yPoint < negY) {
                        negY = yPoint;
                    }

                }
            }
        }
        // log.info("The most negative x is" + negX + " the most negative y is " + negY);
        // log.info("The system length is : " + systemLength + " the system diameter is " + systemDiameter);

        // shift away from negative points so we are in quadrant 1 only
        for (int elementPos = 0; elementPos < dataPoints.system.elements.size(); elementPos++) {
            VisualizationData.InterfaceList element = dataPoints.system.elements.get(elementPos);
            for (int interfacePos = 0; interfacePos < element.interfaces.size(); interfacePos++) {
                VisualizationData.PointsList interfacePoints = element.interfaces.get(interfacePos);
                for (int pointPos = 0; pointPos < interfacePoints.points.length; pointPos++) {
                    Double xPoint = interfacePoints.points[pointPos][0];
                    Double yPoint = interfacePoints.points[pointPos][1];
                    dataPoints.system.elements.get(elementPos).interfaces.get(interfacePos).points[pointPos][0] = xPoint - negX;
                    dataPoints.system.elements.get(elementPos).interfaces.get(interfacePos).points[pointPos][1] = -1 * yPoint - negY;

                    //log.info("x: {} and y: {}",(xPoint - negX),(yPoint-negY));

                }
            }
        }

        // if the rays are to be drawn, we have to shift them as well
        // only get the rays, not marginal or chief
        List<VisualizationData.RayInterfaces> systemRays = new ArrayList<>();
        if (drawRays) {
            systemRays = dataPoints.rayData.rayPoints.get(0).rays;
            for (int ray = 0; ray < systemRays.size(); ray++) {
                List<VisualizationData.RayData> rayData = systemRays.get(ray).rayData;
                for (int rayDataPos = 0; rayDataPos < rayData.size(); rayDataPos++) {
                    Double xPos = rayData.get(rayDataPos).x;
                    Double yPos = rayData.get(rayDataPos).y;
                    systemRays.get(ray).rayData.get(rayDataPos).x = xPos - negX;
                    systemRays.get(ray).rayData.get(rayDataPos).y = -1 * yPos - negY;

                }
            }
        }

        // Goal is to have the max length be at 1000
        Double scaleFactor = height / (2 * systemDiameter);

        if (scaleFactor > width / systemLength) {
            // the system will be way longer than the view window, so change scale factor smaller
            scaleFactor = width / systemLength;
        }

        Double scaledLength = systemLength * scaleFactor + 3 * padding;
        Double scaledDiameter = 2 * (systemDiameter * scaleFactor);


        //  log.info("base diameter = " + systemDiameter + " scaled length is " + scaledLength + " scale factor is " + scaleFactor);
        //FileWriter svgFile = new FileWriter(docName);

        // Extract the source element
        List<VisualizationData.InterfaceList> systemElements = dataPoints.system.elements.subList(0, dataPoints.system.elements.size());

        svgString.append(createHeader(scaledLength, scaledDiameter));

        svgString.append(createSystemDrawing(systemElements, scaleFactor));

        if (drawRays) {
            svgString.append(createRayDrawing(systemRays, scaleFactor));
        }

        svgString.append("\n </svg>");

        // svgFile.close();

        //svgFile.write();

        log.info(svgString.toString());


        return svgString.toString();

    }

    private String createHeader(Double width, Double height) throws IOException {
        String headerString = new String("<svg xmlns=\"http://www.w3.org/2000/svg\"" +
                " version=\"1.1\" width=\"" + width + "\" height=\"" + height + "\">\n");


        return headerString;
    }

    private String createRayDrawing(List<VisualizationData.RayInterfaces> systemRays, Double scaleFactor) {
        StringBuilder rayString = new StringBuilder();

        Integer numberWavelengths = systemRays.get(0).rayData.size() - 1;


        systemRays.forEach(rayInterface -> {
            // start ray svg line
            rayString.append("<path class = \"svg-ray-line\" d= \"M " + formatForSVG(rayInterface.rayData.get(0).x, scaleFactor, padding) +
                    " " + formatForSVG(rayInterface.rayData.get(0).y, scaleFactor, 0.0));
            rayInterface.rayData.forEach(rayData -> {
                rayString.append(" L " + formatForSVG(rayData.x, scaleFactor, padding) + " " + formatForSVG(rayData.y, scaleFactor, 0.0));
            });
            String strokeColor = wavelengthToRGB(rayInterface.rayData.get(0).wavelength);
            rayString.append("\" stroke=\""+ strokeColor + "\" stroke-width=\"2\" fill=\"transparent\"/> \n");

        });


        return rayString.toString();

    }

    private String createSystemDrawing(List<VisualizationData.InterfaceList> dataPoints, Double scaleFactor) {


        StringBuilder systemString = new StringBuilder();
        //log.info("The calculated maximum height is " + maxHeight);

        dataPoints.forEach(interfaceList -> {
                    systemString.append(drawElement(interfaceList, scaleFactor));
                }
        );

        return systemString.toString();
    }

    private String drawElement(VisualizationData.InterfaceList interfaceList, Double scaleFactor) {
        StringBuilder elementString = new StringBuilder();


        // We need a growing string as well that pulls a point from interface to interface
        StringBuilder elementBottom = new StringBuilder();
        StringBuilder elementTop = new StringBuilder();

        // starting bottom points
        Double xBottom = interfaceList.interfaces.get(0).points[0][0];
        Double yBottom = interfaceList.interfaces.get(0).points[0][1];

        Double xTop = interfaceList.interfaces.get(0).points[interfaceList.interfaces.get(0).points.length - 1][0];
        Double yTop = interfaceList.interfaces.get(0).points[interfaceList.interfaces.get(0).points.length - 1][1];

        elementBottom.append("<path class = \"svg-bottom-line\" d= \"M " + formatForSVG(xBottom, scaleFactor, padding) +
                " " + formatForSVG(yBottom, scaleFactor, 0.0));

        elementTop.append("<path class = \"svg-top-line\" d= \"M " + formatForSVG(xTop, scaleFactor, padding) +
                " " + formatForSVG(yTop, scaleFactor, 0.0));

        interfaceList.interfaces.forEach(pointsList -> {
            if (!pointsList.interfaceType.equals("Fictional")) {
                elementString.append(drawInterfaces(pointsList, scaleFactor));

                Double tempXBottom = pointsList.points[0][0];
                Double tempYBottom = pointsList.points[0][1];

                Double tempXTop = pointsList.points[pointsList.points.length - 1][0];
                Double tempYTop = pointsList.points[pointsList.points.length - 1][1];

                elementBottom.append(" L " + formatForSVG(tempXBottom, scaleFactor, padding) + " " + formatForSVG(tempYBottom, scaleFactor, 0.0));
                elementTop.append(" L " + formatForSVG(tempXTop, scaleFactor, padding) + " " + formatForSVG(tempYTop, scaleFactor, 0.0));
            }
            // log.info("We are on a new interface");
        });

        elementBottom.append("\" stroke=\"black\" stroke-width=\"2\" fill=\"transparent\"/> \n");
        elementTop.append("\" stroke=\"black\" stroke-width=\"2\" fill=\"transparent\"/> \n");

        elementString.append(elementTop);
        elementString.append(elementBottom);

        return elementString.toString();
    }


    private String drawInterfaces(VisualizationData.PointsList pointsList, Double scaleFactor) {
        StringBuilder interfaceString = new StringBuilder();

        // convert to surface points
        List<SurfacePoints> surfacePoints = new ArrayList<>();

        Arrays.stream(pointsList.points).forEach(points -> {
            surfacePoints.add(new SurfacePoints(points[0], points[1]));
        });

        interfaceString.append("<path class = \"svg-optic-line\" d= \"M " + formatForSVG(surfacePoints.get(0).xValue, scaleFactor, padding) +
                " " + formatForSVG(surfacePoints.get(0).yValue, scaleFactor, 0.0));


        surfacePoints.subList(1, surfacePoints.size()).forEach(xyPoints -> {
            interfaceString.append(" L " + formatForSVG(xyPoints.xValue, scaleFactor, padding) + " " + formatForSVG(xyPoints.yValue, scaleFactor, 0.0));
        });

        interfaceString.append("\" stroke=\"black\" stroke-width=\"2\" fill=\"transparent\"/> \n");


        return interfaceString.toString();
    }

    private String formatForSVG(double point, Double scaleFactor, @Nullable double shift) {

        Double position = (point * scaleFactor) + shift;

        return (String.format("%.1f", position));
    }

    private String wavelengthToRGB(double wavelength) {


/*
#    Based on code by Dan Bruton
#    http://www.physics.sfasu.edu/astro/color/spectra.html
#    '''

 */

        Double gamma = 0.8;
        Double R = 0.0;
        Double G = 0.0;
        Double B = 0.0;

        // wavelength comes in units of microns, lets convert to nm
        wavelength = wavelength * 1000;

        if (wavelength >= 380 & wavelength <= 440) {
            Double attenuation = 0.3 + 0.7 * (wavelength - 380) / (440 - 380);
            R = Math.pow((-(wavelength - 440) / (440 - 380)) * attenuation, gamma);
            G = 0.0;
            B = Math.pow((1.0 * attenuation), gamma);
        } else if (wavelength >= 440 & wavelength <= 490) {
            R = 0.0;
            G = Math.pow(((wavelength - 440) / (490 - 440)), gamma);
            B = 1.0;
        } else if (wavelength >= 490 & wavelength <= 510) {
            R = 0.0;
            G = 1.0;
            B = Math.pow((-(wavelength - 510) / (510 - 490)), gamma);
        } else if (wavelength >= 510 & wavelength <= 580) {
            R = Math.pow(((wavelength - 510) / (580 - 510)), gamma);
            G = 1.0;
            B = 0.0;
        } else if (wavelength >= 580 & wavelength <= 645) {
            R = 1.0;
            G = Math.pow((-(wavelength - 645) / (645 - 580)), gamma);
            B = 0.0;
        } else if (wavelength >= 645 & wavelength <= 750) {
            Double attenuation = 0.3 + 0.7 * (750 - wavelength) / (750 - 645);
            R = Math.pow((1.0 * attenuation), gamma);
            G = 0.0;
            B = 0.0;
        } else {
            R = 0.2;
            G = 0.2;
            B = 0.2;
        }
        Integer Rint = (int) Math.floor(R * 255);
        Integer Gint = (int) Math.floor(G * 255);
        Integer Bint = (int) Math.floor(B * 255);

        String colorFill = "rgb(" + Rint + "," + Gint + ", " + Bint + ")";
        return colorFill;
    }


    public RenderSVG() {
    }
}
