/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moloud.questionanswering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mabas
 */
public class MainForm extends javax.swing.JFrame {

    /**
     * @param DATASET position of data set
     * @param classes key is the answer and value is a list of questions
     */
    private static final String DATASET = "F:/AI/nlp/QuestionAnswering/dataset/dataset.xml";
    private static final String STOP_WORDS = "F:/AI/nlp/QuestionAnswering/dataset/stopwords.xml";
    private String[] stopWords;
    private HashMap<String, ArrayList<String>> classes = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> train = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> test = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> processedTrain = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> processedTest = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<Integer[]>> trainFeatureVector = new HashMap<String, ArrayList<Integer[]>>();
    private HashMap<String, ArrayList<Integer[]>> testFeatureVector = new HashMap<String, ArrayList<Integer[]>>();
    private HashMap<String, Integer> features = new HashMap<String, Integer>();

    /**
     * Creates new form MainForm
     */
    public MainForm() {
        initComponents();
        getDataset();
        getStopWords();
    }

    /**
     * read data set from local storage and initial classes
     */
    public void getDataset() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(DATASET));
            document.getDocumentElement().normalize();

            NodeList queries = document.getElementsByTagName("Query");
            for (int index = 0; index < queries.getLength(); index++) {
                Node query = queries.item(index);
                if (query.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) query;
                    String question = element.getElementsByTagName("Question").item(0).getTextContent();
                    String answer = element.getElementsByTagName("acceptable-answer").item(0).getTextContent();
                    if (classes.get(answer) != null) {
                        classes.get(answer).add(question);
                    } else {
                        ArrayList<String> questions = new ArrayList<String>();
                        questions.add(question);
                        classes.put(answer, questions);
                    }
                }
            }
            System.out.println(classes.size());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get stop words from local storage we calculated stop words by sorting all
     * the words on 25 documents regard to different regulations
     */
    public void getStopWords() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(STOP_WORDS));
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("root");
            Node node = nodeList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String words = element.getElementsByTagName("Words").item(0).getTextContent();
                stopWords = words.split("[ \n\t\r.,;:!?(){}]");
                System.out.println(stopWords);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * calculate train and test sets the total number of answers is 117 size of
     * the train's set is more than 75% of real database size of the test's set
     * is near 25% of real database there is a possibility for unadequet train
     * set
     */
    public void allTrainTestCalculate() {
        train.clear();
        test.clear();
        ArrayList<String> trainQuestions = new ArrayList<String>();
        ArrayList<String> testQuestions = new ArrayList<String>();
        for (String answer : classes.keySet()) {
            trainQuestions.clear();
            testQuestions.clear();
            switch (classes.get(answer).size()) {
                case 1:
                    train.put(answer, classes.get(answer));
                    break;
                case 2:
                    trainQuestions.add(classes.get(answer).get(0));
                    testQuestions.add(classes.get(answer).get(1));
                    train.put(answer, trainQuestions);
                    test.put(answer, testQuestions);
                    processedTrain.put(answer, trainQuestions);
                    processedTest.put(answer, testQuestions);
                    break;
                case 3:
                    trainQuestions.add(classes.get(answer).get(2));
                    trainQuestions.add(classes.get(answer).get(1));
                    testQuestions.add(classes.get(answer).get(0));
                    train.put(answer, trainQuestions);
                    test.put(answer, testQuestions);
                    processedTrain.put(answer, trainQuestions);
                    processedTest.put(answer, testQuestions);
                    break;
                case 4:
                    trainQuestions.add(classes.get(answer).get(3));
                    trainQuestions.add(classes.get(answer).get(2));
                    trainQuestions.add(classes.get(answer).get(1));
                    testQuestions.add(classes.get(answer).get(0));
                    train.put(answer, trainQuestions);
                    test.put(answer, testQuestions);
                    processedTrain.put(answer, trainQuestions);
                    processedTest.put(answer, testQuestions);
                    break;
                default:
                    int trainSize = (int) (classes.get(answer).size() * 0.75);
                    int testSize = classes.get(answer).size() - trainSize;
                    for (int index = 0; index < testSize; index++) {
                        testQuestions.add(classes.get(answer).get(index));
                    }
                    for (int index = testSize; index < classes.get(answer).size(); index++) {
                        trainQuestions.add(classes.get(answer).get(index));
                    }
                    train.put(answer, trainQuestions);
                    test.put(answer, testQuestions);
                    processedTrain.put(answer, trainQuestions);
                    processedTest.put(answer, testQuestions);
            }

        }
        System.out.println(train.size());
        System.out.println(test.size());
    }

    /**
     * calculate train and test sets the total number of answers is 11 size of
     * the train's set is more than 75% of real database size of the test's set
     * is near 25% of real database be sure there is enough data for train
     */
    public void modifiedTrainTestSet() {
        train.clear();
        test.clear();
        processedTrain.clear();
        processedTest.clear();
        ArrayList<String> trainQuestions = new ArrayList<String>();
        ArrayList<String> testQuestions = new ArrayList<String>();
        for (String answer : classes.keySet()) {
            trainQuestions.clear();
            testQuestions.clear();
            if (classes.get(answer).size() > 4) {
                int trainSize = (int) (classes.get(answer).size() * 0.75);
                int testSize = classes.get(answer).size() - trainSize;
                for (int index = 0; index < testSize; index++) {
                    testQuestions.add(classes.get(answer).get(index));
                }
                for (int index = testSize; index < classes.get(answer).size(); index++) {
                    trainQuestions.add(classes.get(answer).get(index));
                }
                train.put(answer, trainQuestions);
                test.put(answer, testQuestions);
                processedTrain.put(answer, trainQuestions);
                processedTest.put(answer, testQuestions);
            }
        }
        System.out.println(train.size());
        System.out.println(test.size());
    }

    /**
     * in this preprocessing method change all the letter in Arabic or other
     * language similar to Persian into persian letter a good example of this
     * preprocessing can be seen in English letter é would be converted to e
     */
    private String unification(String str) {
        str = str.replace(". ", ".");
        str = str.replace("..", "");
        str = str.replace(":", "");
        str = str.replace("؟", "");
        str = str.replace("؟", "");
        str = str.replace("?", "");
        str = str.replace("،", "");
        str = str.replace("؛", "");
        str = str.replace(" .", ".");
        str = str.replace(") ", " ");
        str = str.replace(")", " ");
        str = str.replace(" )", " ");
        str = str.replace("( ", " ");
        str = str.replace("(", " ");
        str = str.replace(" (", " ");
        str = str.replace("  ", " ");
        str = str.replace("   ", " ");
        str = str.replace("\n", "");
        str = str.replace("ك", "ک");
        str = str.replace("ي", "ی");
        str = str.replace("ئ", "ی");
        str = str.replace("ؤ", "و");
        str = str.replace("ء", "");
        str = str.replace("\n", "");
        str = str.replace("-", "");
        str = str.replace("- ", "");
        str = str.replace(" -", "");
        str = str.replace("[", "");
        str = str.replace("]", "");
        str = str.replace("{", "");
        str = str.replace("}", "");
        str = str.replace("  ", "");
        str = str.replace("  ", "");
        str = str.replace("أ", "ا");
        str = str.replace("ــ", "");
        return str;
    }

   /**
     * this function find every word on every questions in train set, then build
     * the feature vector. after that, find number of occurrence of each word on
     * questions on both train and test sets
     */
    public void buildUnigrams() {
        // calculate all the words inquestion
        trainFeatureVector.clear();
        features.clear();
        for (String answer : processedTrain.keySet()) {
            for (String question : processedTrain.get(answer)) {
                String[] questionWords = question.split(" ");
                for (String word : questionWords) {
                    if (features.get(word) == null) {
                        features.put(word, 1);
                    }
                }
            }
        }
        for (String answer : processedTrain.keySet()) {
            ArrayList<Integer[]> questions = new ArrayList<Integer[]>();
            for (String question : processedTrain.get(answer)) {
                String[] questionWords = question.split(" ");
                Integer[] questionFeatureVector = new Integer[features.size()];
                for (int itrator = 0; itrator < questionFeatureVector.length; itrator++) {
                    questionFeatureVector[itrator] = 0;
                }
                int index = 0;
                for (String word : features.keySet()) {
                    for (String questionWord : questionWords) {
                        if (word.equals(questionWord)) {
                            questionFeatureVector[index] = questionFeatureVector[index] + 1;
                        }
                    }
                    index++;
                }
                questions.add(questionFeatureVector);
            }
            trainFeatureVector.put(answer, questions);
        }
        for (String answer : processedTest.keySet()) {
            ArrayList<Integer[]> questions = new ArrayList<Integer[]>();
            for (String question : processedTest.get(answer)) {
                String[] questionWords = question.split(" ");
                Integer[] questionFeatureVector = new Integer[features.size()];
                for (int itrator = 0; itrator < questionFeatureVector.length; itrator++) {
                    questionFeatureVector[itrator] = 0;
                }
                int index = 0;
                for (String word : features.keySet()) {
                    for (String questionWord : questionWords) {
                        if (word.equals(questionWord)) {
                            questionFeatureVector[index] = questionFeatureVector[index] + 1;
                        }
                    }
                    index++;
                }
                questions.add(questionFeatureVector);
            }
            testFeatureVector.put(answer, questions);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        txt_question = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txt_question1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        checkBox_stopword = new javax.swing.JCheckBox();
        checkBox_unification = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        rbtn_unigram = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txt_question.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_questionActionPerformed(evt);
            }
        });

        jLabel1.setText("Please ask your question?");
        jLabel1.setName("Please ask your question?"); // NOI18N

        txt_question1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_question1ActionPerformed(evt);
            }
        });

        jLabel2.setText("here is your answer");
        jLabel2.setName("Please ask your question?"); // NOI18N

        jButton1.setText("jButton1");

        jLayeredPane1.setLayer(txt_question, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(txt_question1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel2, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jButton1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addComponent(txt_question, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap(41, Short.MAX_VALUE)
                    .addComponent(txt_question1, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(28, 28, 28)))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(31, 31, 31)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txt_question, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addGap(74, 74, 74)
                    .addComponent(txt_question1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(312, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Database", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));
        jPanel2.setToolTipText("");
        jPanel2.setName("data"); // NOI18N

        jRadioButton1.setText("all data");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton2.setText("remove single questionsions");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Preprocessing"));

        checkBox_stopword.setText("remove stopwords");
        checkBox_stopword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBox_stopwordActionPerformed(evt);
            }
        });

        checkBox_unification.setText("unification");
        checkBox_unification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBox_unificationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBox_stopword)
                    .addComponent(checkBox_unification))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(checkBox_unification, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBox_stopword)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 38, Short.MAX_VALUE)
                        .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(76, 76, 76)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(17, 17, 17)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel2.getAccessibleContext().setAccessibleName("Database");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txt_questionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_questionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_questionActionPerformed

    private void txt_question1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_question1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_question1ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        allTrainTestCalculate();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        modifiedTrainTestSet();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void checkBox_stopwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBox_stopwordActionPerformed
        if (checkBox_stopword.isSelected()) {
            HashMap<String, ArrayList<String>> tempTrain = new HashMap<String, ArrayList<String>>();
            HashMap<String, ArrayList<String>> tempTest = new HashMap<String, ArrayList<String>>();
            for (String answer : processedTrain.keySet()) {
                String proccedAnswer = answer;
                ArrayList<String> proccedQuestions = new ArrayList<String>();
                for (String question : processedTrain.get(answer)) {
                    for (String word : stopWords) {
                        question = question.replaceAll(word, "");
                    }
                    proccedQuestions.add(question);
                }
                for (String word : stopWords) {
                    proccedAnswer = proccedAnswer.replaceAll(word, "");
                }
                tempTrain.put(proccedAnswer, proccedQuestions);
            }
            for (String answer : processedTest.keySet()) {
                String proccedAnswer = answer;
                ArrayList<String> proccedQuestions = new ArrayList<String>();
                for (String question : processedTest.get(answer)) {
                    for (String word : stopWords) {
                        question = question.replaceAll(word, "");
                    }
                    proccedQuestions.add(question);
                }
                for (String word : stopWords) {
                    proccedAnswer = proccedAnswer.replaceAll(word, "");
                }
                tempTest.put(proccedAnswer, proccedQuestions);
            }
            processedTrain.clear();
            processedTest.clear();
            processedTrain.putAll(tempTrain);
            processedTest.putAll(tempTest);
        } else {
            processedTrain.clear();
            processedTest.clear();
            processedTrain.putAll(train);
            processedTest.putAll(test);
        }
    }//GEN-LAST:event_checkBox_stopwordActionPerformed

    private void checkBox_unificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBox_unificationActionPerformed
        if (checkBox_unification.isSelected()) {
            HashMap<String, ArrayList<String>> tempTrain = new HashMap<String, ArrayList<String>>();
            HashMap<String, ArrayList<String>> tempTest = new HashMap<String, ArrayList<String>>();
            for (String answer : processedTrain.keySet()) {
                String proccedAnswer = answer;
                ArrayList<String> proccedQuestions = new ArrayList<String>();
                for (String question : processedTrain.get(answer)) {
                    proccedQuestions.add(unification(question));
                }
                proccedAnswer = unification(answer);
                tempTrain.put(proccedAnswer, proccedQuestions);
            }
           for (String answer : processedTest.keySet()) {
                String proccedAnswer = answer;
                ArrayList<String> proccedQuestions = new ArrayList<String>();
                for (String question : processedTest.get(answer)) {
                    proccedQuestions.add(unification(question));
                }
                proccedAnswer = unification(answer);
                tempTrain.put(proccedAnswer, proccedQuestions);
            }
            processedTrain.clear();
            processedTest.clear();
            processedTrain.putAll(tempTrain);
            processedTest.putAll(tempTest);
        } else {
            processedTrain.clear();
            processedTest.clear();
            processedTrain.putAll(train);
            processedTest.putAll(test);
        }
    }//GEN-LAST:event_checkBox_unificationActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox checkBox_stopword;
    private javax.swing.JCheckBox checkBox_unification;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JTextField txt_question;
    private javax.swing.JTextField txt_question1;
    // End of variables declaration//GEN-END:variables
}
