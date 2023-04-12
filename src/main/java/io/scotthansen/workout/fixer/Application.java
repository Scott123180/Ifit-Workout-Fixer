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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final String FILE_PATH = "src/main/resources/workout/workout.tcx";

    private static final double INPUT_ACTUAL_CALORIES = 1;

    public static void main(final String[] args) throws ParserConfigurationException, TransformerException {

        File file = new File(FILE_PATH);
        Data data = parseTcxFile(file);

        writeTcxFile(data, new File("new_workout.tcx"));

        System.out.println("hi");
    }

    public static Data parseTcxFile(File inputFile) {
        List<TrackPoint> trackPoints = new ArrayList<>();
        int totalTimeSeconds = 0;

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

            // Get the Lap element
            Element lap = (Element) activityElement.getElementsByTagName("Lap").item(0);

            // Get the value of the TotalTimeSeconds element
            totalTimeSeconds = Integer.parseInt(lap.getElementsByTagName("TotalTimeSeconds").item(0).getTextContent());

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

        return Data
                .builder()
                .trackPointList(trackPoints)
                .totalTimeSeconds(totalTimeSeconds)
                .build();
    }

    public static void writeTcxFile(Data data, File outputFile)
            throws ParserConfigurationException, TransformerException {

        final int totalTimeSeconds = data.totalTimeSeconds;
        List<TrackPoint> trackPoints = data.trackPointList;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // create root element "TrainingCenterDatabase"
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("TrainingCenterDatabase");
        doc.appendChild(rootElement);

        // create "Activities" element and append to root
        Element activitiesElement = doc.createElement("Activities");
        rootElement.appendChild(activitiesElement);

        // create "Activity" element and append to "Activities"
        Element activityElement = doc.createElement("Activity");
        activityElement.setAttribute("Sport", "biking");
        activitiesElement.appendChild(activityElement);

        // create "Id" element and append to "Activity"
        Element idElement = doc.createElement("Id");
        String startTime = trackPoints.get(0).time;

        idElement.setTextContent(startTime);
        activityElement.appendChild(idElement);

        // create "Lap" element and append to "Activity"
        Element lapElement = doc.createElement("Lap");
        lapElement.setAttribute("StartTime", startTime);
        lapElement.setAttribute("TotalTimeSeconds", String.format("%d", totalTimeSeconds));
        lapElement.setAttribute("DistanceMeters", String.format("%.1f", trackPoints.get(trackPoints.size() - 1).distanceMeters));
        lapElement.setAttribute("Calories", String.format("%.0f", trackPoints.get(trackPoints.size() - 1).calories));
        lapElement.setAttribute("Intensity", "Active");
        lapElement.setAttribute("TriggerMethod", "Manual");
        activityElement.appendChild(lapElement);

        // create "Track" element and append to "Lap"
        Element trackElement = doc.createElement("Track");
        lapElement.appendChild(trackElement);

        // create "Trackpoint" elements and append to "Track"
        for (TrackPoint tp : trackPoints) {
            Element trackPointElement = doc.createElement("Trackpoint");
            trackElement.appendChild(trackPointElement);

            Element timeElement = doc.createElement("Time");
            timeElement.setTextContent(tp.time);
            trackPointElement.appendChild(timeElement);

            Element distanceElement = doc.createElement("DistanceMeters");
            distanceElement.setTextContent(String.format("%.1f", tp.distanceMeters));
            trackPointElement.appendChild(distanceElement);

            Element cadenceElement = doc.createElement("Cadence");
            cadenceElement.setTextContent(Integer.toString(tp.cadence));
            trackPointElement.appendChild(cadenceElement);
        }

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);

        transformer.transform(source, result);

        System.out.println("TCX file saved to " + outputFile.getAbsolutePath());
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