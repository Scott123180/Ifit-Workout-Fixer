package io.scotthansen.workout.fixer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final String FILE_PATH = "src/main/resources/workout/workout.tcx";

    private static final double INPUT_ACTUAL_CALORIES = 1;

    public static void main(final String[] args) {

        File file = new File(FILE_PATH);
        List<TrackPoint> trackPoints = parseTcxFile(file);

        System.out.println("hi");
    }

    public static List<TrackPoint> parseTcxFile(File inputFile) {
        List<TrackPoint> trackPoints = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList activityList = doc.getElementsByTagName("Activity");
            Node activityNode = activityList.item(0);
            Element activityElement = (Element) activityNode;
            String sport = activityElement.getAttribute("Sport");
            String startTime = activityElement.getElementsByTagName("Id").item(0).getTextContent();

            NodeList lapList = doc.getElementsByTagName("Lap");
            int lapCount = lapList.getLength();

            NodeList trackPointList = doc.getElementsByTagName("Trackpoint");
            for (int i = 0; i < trackPointList.getLength(); i++) {
                Node trackPointNode = trackPointList.item(i);
                if (trackPointNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element trackPointElement = (Element) trackPointNode;
                    String time = trackPointElement.getElementsByTagName("Time").item(0).getTextContent();

                    Element distanceElement = (Element) trackPointElement.getElementsByTagName("DistanceMeters").item(0);
                    double distanceMeters = distanceElement != null ? Double.parseDouble(distanceElement.getTextContent()) : 0.0;

                    Element heartRateElement = (Element) trackPointElement.getElementsByTagName("HeartRateBpm").item(0);
                    int heartRateBpm = heartRateElement != null ? Integer.parseInt(heartRateElement.getElementsByTagName("Value").item(0).getTextContent()) : 0;

                    Element cadenceElement = (Element) trackPointElement.getElementsByTagName("Cadence").item(0);
                    int cadence = cadenceElement != null ? Integer.parseInt(cadenceElement.getTextContent()) : 0;

                    Element caloriesElement = (Element) trackPointElement.getElementsByTagName("Calories").item(0);
                    double calories = caloriesElement != null ? Double.parseDouble(caloriesElement.getTextContent()) : 0.0;

                    trackPoints.add(new TrackPoint(time, distanceMeters, cadence, heartRateBpm, calories));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trackPoints;
    }
}

class TrackPoint {
    String time;
    double distanceMeters;
    int cadence;
    int heartRateBpm;
    double calories;

    public TrackPoint(String time, double distanceMeters, int cadence, int heartRateBpm, double calories) {
        this.time = time;
        this.distanceMeters = distanceMeters;
        this.cadence = cadence;
        this.heartRateBpm = heartRateBpm;
        this.calories = calories;
    }
}