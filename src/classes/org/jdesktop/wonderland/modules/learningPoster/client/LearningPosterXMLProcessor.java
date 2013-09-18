/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.learningPoster.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.spi.DirStateFactory.Result;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Joey
 */
public class LearningPosterXMLProcessor {

    private static final Logger LOGGER =
            Logger.getLogger(LearningPosterXMLProcessor.class.getName());
    static String ROOT_ELEMENT_NAME = "learningData";
    static String FILENAME = "learningData.xml";
    static String ANSWERS_TAG = "answers";
    static String QUESTION_TAG = "Question";
    static String USER_TAG = "User";
    static String QUESTION_TEXT_TAG = "QuestionText";
    static String USER_NAME = "userName";

    /**
     * @param args the command line arguments
     */
    static public File makeDemo(String filename) throws TransformerConfigurationException, TransformerException, PropertyException, JAXBException, SAXException, IOException, ParserConfigurationException {

        //Document doc = createNewDocument();
        Document doc = getDocument(filename);
        Element rootElement = doc.getDocumentElement();

        Element question = addQuestion(rootElement, "this is a testquestion?", "D");
        addAnswer(question, "answer 1", true);
        addAnswer(question, "answer 2", false);
        addAnswer(question, "answer 3", false);

        Element question2 = addQuestion(rootElement, "this is second  testquestion?", "C");
        addAnswer(question2, "answer 1", true);
        addAnswer(question2, "answer 2", false);
        addAnswer(question2, "answer 5", false);

        //  Element user = addUser(rootElement, "Joey");
//      Element user = getUser(rootElement, "Peter", null);
        //  addUserAnswer(user, "Q1A1", null);
        //  addUserAnswer(user, "Q1A2", null);

        return writeFileFromDoc(doc, filename);

        /*
        HashMap questionMap = createQuestionArray(rootElement);
        String htmlTag = createHTMLString(rootElement, "Q1");*/
    }
    /**
     * @param args the command line arguments
     */
    static public File makeDemo2(String filename) throws TransformerConfigurationException, TransformerException, PropertyException, JAXBException, SAXException, IOException, ParserConfigurationException {

        //Document doc = createNewDocument();
        Document doc = getDocument(filename);
        Element rootElement = doc.getDocumentElement();

        Element question = addQuestion(rootElement, "this is a testquestion?", "D");
        addAnswer(question, "answer 100", true);
        addAnswer(question, "answer 26", false);
        addAnswer(question, "answer 35", false);

        Element question2 = addQuestion(rootElement, "this is second  testquestion?", "C");
        addAnswer(question2, "answer 1456", true);
        addAnswer(question2, "answer 24", false);
        addAnswer(question2, "answer 54", false);

        //  Element user = addUser(rootElement, "Joey");
//      Element user = getUser(rootElement, "Peter", null);
        //  addUserAnswer(user, "Q1A1", null);
        //  addUserAnswer(user, "Q1A2", null);

        return writeFileFromDoc(doc, filename);

        /*
        HashMap questionMap = createQuestionArray(rootElement);
        String htmlTag = createHTMLString(rootElement, "Q1");*/
    }



    /**
     * @param args the command line arguments
     */
    static public File addElementToFile(String filename, Element element) throws TransformerConfigurationException, TransformerException, PropertyException, JAXBException, SAXException, IOException, ParserConfigurationException {

        //Document doc = createNewDocument();
        Document doc = getDocument(filename);
        Element rootElement = doc.getDocumentElement();

        rootElement.appendChild(element);
        return writeFileFromDoc(doc, filename);

        /*
        HashMap questionMap = createQuestionArray(rootElement);
        String htmlTag = createHTMLString(rootElement, "Q1");*/
    }

    public static Element getRootElementFromFile(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);
        Element rootElement = doc.getDocumentElement();
        return rootElement;
    }

    public static void addAnswer(Element question, String answerText, boolean isTrue) {
        LOGGER.log(Level.WARNING, "addAnswer");
        int actualAnswerIDMax = 0;
        for (int i = 0; i < question.getElementsByTagName(ANSWERS_TAG).getLength(); i++) {
            int attributeID =
                    Integer.valueOf(question.getElementsByTagName(ANSWERS_TAG).item(i).
                    getAttributes().getNamedItem("id").getTextContent().split("A")[1]);
            if (attributeID != 0) {
                actualAnswerIDMax = attributeID;
            }
        }

        actualAnswerIDMax = actualAnswerIDMax == 0 ? 1 : actualAnswerIDMax + 1;

        String answerIDString =
                question.getAttribute("id") + "A" + String.valueOf(actualAnswerIDMax);

        //firstname elements
        Element answersElement = question.getOwnerDocument().createElement(ANSWERS_TAG);
        answersElement.appendChild(question.getOwnerDocument().createTextNode(answerText));
        answersElement.setAttribute("id", answerIDString);
        answersElement.setAttribute("true", String.valueOf(isTrue));
        question.appendChild(answersElement);
    }

    public static Element addQuestion(Element rootElement, String questionText, String questionType) {

        int actualQuestionIDMax = 0;
        for (int i = 0; i < rootElement.getElementsByTagName(QUESTION_TAG).getLength(); i++) {
            int attributeID =
                    Integer.valueOf(rootElement.getElementsByTagName(QUESTION_TAG).item(i).
                    getAttributes().getNamedItem("id").getTextContent().split("Q")[1]);
            if (attributeID != 0) {
                actualQuestionIDMax = attributeID;
            }
        }

        actualQuestionIDMax = actualQuestionIDMax == 0 ? 1 : actualQuestionIDMax + 1;

        String questionIDString = "Q" + String.valueOf(actualQuestionIDMax);

        Element question = rootElement.getOwnerDocument().createElement(QUESTION_TAG);
        rootElement.appendChild(question);


        Attr attr = rootElement.getOwnerDocument().createAttribute("id");
        attr.setValue(questionIDString);
        question.setAttributeNode(attr);

        Attr type = rootElement.getOwnerDocument().createAttribute("type");
        type.setValue(questionType);
        question.setAttributeNode(type);

        Element questionTextElement = rootElement.getOwnerDocument().createElement(QUESTION_TEXT_TAG);
        questionTextElement.appendChild(rootElement.getOwnerDocument().createTextNode(questionText));
        question.appendChild(questionTextElement);

        return question;
    }

    public static Element addUser(Element rootElement, String userName) {

        int actualUserIDMax = 0;
        for (int i = 0; i < rootElement.getElementsByTagName(USER_TAG).getLength(); i++) {
            int attributeID =
                    Integer.valueOf(rootElement.getElementsByTagName(USER_TAG).item(i).
                    getAttributes().getNamedItem("id").getTextContent().split("U")[1]);
            if (attributeID != 0) {
                actualUserIDMax = attributeID;
            }
        }

        actualUserIDMax = actualUserIDMax == 0 ? 1 : actualUserIDMax + 1;

        String questionIDString = "U" + String.valueOf(actualUserIDMax);
        Element question = rootElement.getOwnerDocument().createElement(USER_TAG);
        rootElement.appendChild(question);
        Attr attr = rootElement.getOwnerDocument().createAttribute("id");
        attr.setValue(questionIDString);
        question.setAttributeNode(attr);

        Element questionTextElement = rootElement.getOwnerDocument().createElement(USER_NAME);
        questionTextElement.appendChild(rootElement.getOwnerDocument().createTextNode(userName));
        question.appendChild(questionTextElement);

        return question;
    }

    public static void addUserAnswer(Element user, String answerID, Timestamp timestamp) {

        if (timestamp == null) {
            java.util.Date date = new java.util.Date();
            timestamp = new Timestamp(date.getTime());

        }

        Element answersElement = user.getOwnerDocument().createElement("answers");
        answersElement.appendChild(user.getOwnerDocument().createTextNode(timestamp.toString()));
        answersElement.setAttribute("id", answerID);

        boolean isTrue = checkAnswer(answerID, user);
        answersElement.setAttribute("true", String.valueOf(isTrue));
        user.appendChild(answersElement);
    }

    public static boolean checkAnswer(String answerID, Element user) {
        Boolean questionAnswer = null;
        String questionID = answerID.split("A")[0];
        Document doc = user.getOwnerDocument();
        NodeList questionList = doc.getElementsByTagName(QUESTION_TAG);
        for (int i = 0; i < questionList.getLength(); i++) {
            if (questionList.item(i).getAttributes().getNamedItem("id").getTextContent() == null ? questionID == null : questionList.item(i).getAttributes().getNamedItem("id").getTextContent().equals(questionID)) {
                NodeList answerList = questionList.item(i).getChildNodes();
                for (int j = 0; j < answerList.getLength(); j++) {
                    if (answerList.item(j).getNodeName().equals(ANSWERS_TAG)) {
                        if (answerList.item(j).getAttributes().getNamedItem("id").getTextContent() == null ? answerID == null : answerList.item(j).getAttributes().getNamedItem("id").getTextContent().equals(answerID)) {
                            questionAnswer = Boolean.parseBoolean(answerList.item(j).getAttributes().getNamedItem("true").getTextContent());
                        }
                    }
                }
            }
        }
        return questionAnswer;
    }

    public static Document createNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement(ROOT_ELEMENT_NAME);
        doc.appendChild(rootElement);
        return doc;
    }

    public static Document getDocument(String filename) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        // File f = new File()
        File f = new File(filename);
        if (f.exists()) {
            Document doc = docBuilder.parse(new File(filename));
            return doc;

        } else {
            Document doc = createNewDocument();
            return doc;
        }
    }


    /*
    public static Document addToDocument(URI filename) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    // File f = new File()
    File f = new File(filename);
    if (f.exists()) {
    Document doc = docBuilder.parse(new File(filename));
    return doc;

    } else {
    Document doc = createNewDocument();
    return doc;
    }
    }

     */
    public static File writeFileFromDoc(Document doc, String filename) throws TransformerConfigurationException, TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        File file = new File(filename);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
        return file;
    }

    public static void writeFile(Document doc, String filename) throws TransformerConfigurationException, TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filename));
        transformer.transform(source, result);
    }

    public static Element getUser(Element rootElement, String userName, String userID) {
        NodeList userList = rootElement.getOwnerDocument().getElementsByTagName(USER_TAG);

        for (int i = 0; i < userList.getLength(); i++) {

            if (userID != null) {
                if (userList.item(i).getAttributes().getNamedItem("id").equals(userID)) {
                    for (int j = 0; j < userList.item(i).getChildNodes().getLength(); j++) {
                        if (userList.item(i).getChildNodes().item(j).getNodeName().equals(USER_NAME)) {
                            if (userName.equals(userList.item(i).getChildNodes().item(j).getTextContent())) {
                                return (Element) userList.item(i);
                            }
                        }
                    }
                }
            } else {
                for (int j = 0; j < userList.item(i).getChildNodes().getLength(); j++) {
                    if (userList.item(i).getChildNodes().item(j).getNodeName().equals(USER_NAME)) {
                        if (userName.equals(userList.item(i).getChildNodes().item(j).getTextContent())) {
                            return (Element) userList.item(i);
                        }
                    }
                }
            }
        }
        return addUser(rootElement, userName);
    }

    public static HashMap createQuestionArray(Element rootElement) {
        HashMap<String, String> map = new HashMap<String, String>();
        NodeList questionList = rootElement.getOwnerDocument().getElementsByTagName(QUESTION_TAG);

        for (int i = 0; i < questionList.getLength(); i++) {
            for (int j = 0; j < questionList.item(i).getChildNodes().getLength(); j++) {
                if (questionList.item(i).getChildNodes().item(j).getNodeName().equals(QUESTION_TEXT_TAG)) {
                    map.put(questionList.item(i).getAttributes().getNamedItem("id").getTextContent(),
                            questionList.item(i).getChildNodes().item(j).getTextContent());
                }
            }
        }
        return map;
    }

    public static String createHTMLString(Element rootElement, String questionID) {
        String questionType = "A";
        String bgcolor = "green";
        NodeList questionList = rootElement.getOwnerDocument().getElementsByTagName(QUESTION_TAG);
        for (int i = 0; i < questionList.getLength(); i++) {
            if (questionList.item(i).getAttributes().getNamedItem("id").getTextContent().toString().equals(questionID)) {
                questionType = questionList.item(i).getAttributes().getNamedItem("type").getTextContent().toString();
                LOGGER.severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!" + questionType);
            }
        }
        LOGGER.severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!+++++++++++++++++" + questionType);
        if (questionType.startsWith("C")) {
            questionType = "Concept Question";
            bgcolor = "red";
        } else if (questionType.startsWith("D")) {
            questionType = "Group Discussion";
            bgcolor = "blue";
        }

        String htmlString = "<html><body bgcolor=\"" + bgcolor + "\"><p>"
                + "<p style=\"font-size:2.0em;\" color=\"white\" align=\"center\">"
                + questionType + "</p><center>"
                + "<table bgcolor=\"white\" width=\"85%\" cellpadding=\"20\"><tr><td>" + questionID + "</td><td><h1>";
        System.out.println(questionID);

        HashMap<String, String> answerMap = new HashMap<String, String>();

        for (int i = 0; i < questionList.getLength(); i++) {
            if (questionList.item(i).getAttributes().getNamedItem("id").getTextContent().toString().equals(questionID)) {
                for (int j = 0; j < questionList.item(i).getChildNodes().getLength(); j++) {
                    if (questionList.item(i).getChildNodes().item(j).getNodeName().equals(QUESTION_TEXT_TAG)) {
                        System.out.println(questionList.item(i).getChildNodes().item(j).getTextContent());
                        htmlString = htmlString.concat(questionList.item(i).getChildNodes().item(j).getTextContent());
                        htmlString = htmlString.concat("</h1></td></tr>");
                    }
                    if (questionList.item(i).getChildNodes().item(j).getNodeName().equals(ANSWERS_TAG)) {
                        answerMap.put(questionList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("id").getTextContent(), questionList.item(i).getChildNodes().item(j).getTextContent());
                    }

                }
            }
        }


        Iterator iterator = answerMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            htmlString = htmlString.concat("<tr><td></td><td><a href=\"?answer=" + key + "\">" + answerMap.get(key) + "</a></td></tr>");
        }
        htmlString = htmlString.concat("</table></center><br></body></html>");
        return htmlString;
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

    public static String domToString(Document doc) {
        String content = "";
        try {

            //Console output
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();
            Source src = new DOMSource(doc);
            StringWriter stringWriterOutput = new StringWriter();
            StreamResult dest = new StreamResult(stringWriterOutput);
            aTransformer.transform(src, dest);
            content = stringWriterOutput.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return content;
    }

    public static String filePathToString(String path) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String s;
        String endString = "";
        while ((s = br.readLine()) != null) {
            endString = endString.concat(s);
        }
        fr.close();
        return endString;
    }
}
