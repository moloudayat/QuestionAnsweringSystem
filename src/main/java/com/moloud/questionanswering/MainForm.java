/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moloud.questionanswering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.core.*;
/**
 *
 * @author mabas
 */
public class MainForm extends javax.swing.JFrame {

    /**
     * @param DATASET position of data set
     * @param STOP_WORDS location of stop words
     * @param stopWords list of stopwords
     * @param classes key is the answer and value is a list of questions
     * @param train a hash table that answers are the keys and questions
     * regardig to the keys are values
     * @param test same as train set
     * @param processedTrain the train set after preProcessing
     * @param processedTest same as processedTrain
     * @param trainFeatureVector all the feature vectors of train questions
     * @param testFeatureVector all the feature vectors of test questions
     * @param features feature vectors
     */
    private static final String DATASET = "F:/AI/nlp/QuestionAnswering/dataset/dataset.xml";
    private static final String STOP_WORDS = "F:/AI/nlp/QuestionAnswering/dataset/stopwords.xml";
    private static final String TRAIN_ARFF = "F:/AI/nlp/QuestionAnswering/train.arff";
    private static final String TEST_ARFF = "F:/AI/nlp/QuestionAnswering/test.arff";
    private String[] stopWords;
    private HashMap<String, ArrayList<String>> classes = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> train = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> test = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> processedTrain = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> processedTest = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<Integer[]>> trainFeatureVector = new HashMap<String, ArrayList<Integer[]>>();
    private HashMap<String, ArrayList<Integer[]>> testFeatureVector = new HashMap<String, ArrayList<Integer[]>>();
    private HashMap<String, Integer> features = new HashMap<String, Integer>();
    private String[] answers;

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
        answers = new String[train.size()];
        int index = 0;
        for (String answer : train.keySet()) {
            answers[index++] = answer;
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
        answers = new String[train.size()];
        int index = 0;
        for (String answer : train.keySet()) {
            answers[index++] = answer;
        }
        System.out.println(train.size());
        System.out.println(test.size());
    }

    /**
     * in this preProcessing method change all the letter in Arabic or other
     * language similar to Persian into Persian letter a good example of this
     * preProcessing can be seen in English letter é would be converted to e
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
     * questions on both train and test sets. then for train and test sets build
     * arff files
     */
    public void buildUnigrams() {
        // calculate all the words inquestion
        trainFeatureVector.clear();
        features.clear();
        for (int itrator = 0; itrator < answers.length; itrator++) {
            if (processedTrain.get(answers[itrator]) != null) {
                for (String question : processedTrain.get(answers[itrator])) {
                    String[] questionWords = question.split(" ");
                    for (String word : questionWords) {
                        if (features.get(word) == null) {
                            features.put(word, 1);
                        }
                    }
                }
            }
        }
        for (int itrator1 = 0; itrator1 < answers.length; itrator1++) {
            ArrayList<Integer[]> questions = new ArrayList<Integer[]>();
            if (processedTrain.get(answers[itrator1]) != null) {
                for (String question : processedTrain.get(answers[itrator1])) {
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
                trainFeatureVector.put(answers[itrator1], questions);
            }
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
        try {
            BufferedWriter trainFile = new BufferedWriter(new FileWriter("train.arff"));
            trainFile.write("@relation train");
            trainFile.write(System.getProperty("line.separator"));
            trainFile.write(System.getProperty("line.separator"));
            int attribute = 1;
            for (String word : features.keySet()) {
                trainFile.write("@attribute feature" + attribute + " numeric");
                attribute++;
                trainFile.write(System.getProperty("line.separator"));
            }
            trainFile.write("@attribute class {");
            for (int iterator = 0; iterator < answers.length - 1; iterator++) {
                trainFile.write("answer" + iterator + " ,");
            }
            trainFile.write(answers.length - 1 + "}");
            trainFile.write(System.getProperty("line.separator"));
            trainFile.write(System.getProperty("line.separator"));
            trainFile.write("@data");
            trainFile.write(System.getProperty("line.separator"));
            for (int iterator = 0; iterator < answers.length - 1; iterator++) {
                if (trainFeatureVector.get(answers[iterator]) != null) {
                    for (Integer[] questionVector : trainFeatureVector.get(answers[iterator])) {
                        for (int index = 0; index < questionVector.length; index++) {
                            trainFile.write(questionVector[index] + ",");
                        }
                        trainFile.write("answer" + iterator);
                        trainFile.write(System.getProperty("line.separator"));
                    }
                }
            }
            trainFile.close();
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedWriter testFile = new BufferedWriter(new FileWriter("test.arff"));
            testFile.write("@relation test");
            testFile.write(System.getProperty("line.separator"));
            testFile.write(System.getProperty("line.separator"));
            int attribute = 1;
            for (String word : features.keySet()) {
                testFile.write("@attribute feature" + attribute + " numeric");
                attribute++;
                testFile.write(System.getProperty("line.separator"));
            }
            testFile.write("@attribute class {");
            for (int iterator = 0; iterator < answers.length - 1; iterator++) {
                testFile.write("answer" + iterator + " ,");
            }
            testFile.write(answers.length - 1 + "}");
            testFile.write(System.getProperty("line.separator"));
            testFile.write(System.getProperty("line.separator"));
            testFile.write("@data");
            testFile.write(System.getProperty("line.separator"));
            for (int iterator = 0; iterator < answers.length - 1; iterator++) {
                if (testFeatureVector.get(answers[iterator]) != null) {
                    for (Integer[] questionVector : testFeatureVector.get(answers[iterator])) {
                        for (int index = 0; index < questionVector.length; index++) {
                            testFile.write(questionVector[index] + ",");
                        }
                        testFile.write("answer" + iterator);
                        testFile.write(System.getProperty("line.separator"));
                    }
                }
            }
            testFile.close();
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
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
        rbn_logestic = new javax.swing.JRadioButton();
        rbn_naiveBayse = new javax.swing.JRadioButton();
        etxt_percision = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
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

        jButton1.setText("answer");

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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Classifire"));

        rbn_logestic.setText("Logestic");
        rbn_logestic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbn_logesticActionPerformed(evt);
            }
        });

        rbn_naiveBayse.setText("Naive Bayse");
        rbn_naiveBayse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbn_naiveBayseActionPerformed(evt);
            }
        });

        etxt_percision.setEditable(false);
        etxt_percision.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        etxt_percision.setSelectionColor(new java.awt.Color(0, 0, 0));

        jLabel3.setText("percision:");

        jLabel4.setText("%");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbn_logestic)
                    .addComponent(rbn_naiveBayse)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(etxt_percision, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbn_logestic)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbn_naiveBayse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(etxt_percision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addContainerGap())
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
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addContainerGap())
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Features"));

        rbtn_unigram.setText("unigram");
        rbtn_unigram.setContentAreaFilled(false);
        rbtn_unigram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtn_unigramActionPerformed(evt);
            }
        });

        jRadioButton3.setText("bigram");

        jRadioButton4.setText("trigram");

        jRadioButton5.setText("tf-idf");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbtn_unigram)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rbtn_unigram)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton5)
                .addGap(32, 32, 32))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 23, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLayeredPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );

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
    }//GEN-LAST:event_checkBox_unificationActionPerformed

    private void rbtn_unigramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtn_unigramActionPerformed
        if (rbtn_unigram.isSelected()) {
            buildUnigrams();
        }
    }//GEN-LAST:event_rbtn_unigramActionPerformed

    private void rbn_logesticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbn_logesticActionPerformed
        if (rbn_logestic.isSelected()) {
            try {
                Instances trainSet = new Instances(new BufferedReader(new FileReader(TRAIN_ARFF)));
                Instances testSet = new Instances(new BufferedReader(new FileReader(TEST_ARFF)));
                trainSet.setClassIndex(trainSet.numAttributes() - 1);
                testSet.setClassIndex(testSet.numAttributes() - 1);
                Logistic logistic = new Logistic();
                System.out.print("Training on " + trainSet.size() + " examples... ");
                logistic.buildClassifier(trainSet);
                System.out.println("done.");
                Evaluation eval = new Evaluation(testSet);
                eval.evaluateModel(logistic, testSet);
                System.out.println(eval.toSummaryString("\nResults\n======\n", true));

            } catch (Exception ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Unable to train classifier.");
                System.err.println("\t" + ex.getMessage());
            }
        }
    }//GEN-LAST:event_rbn_logesticActionPerformed

    private void rbn_naiveBayseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbn_naiveBayseActionPerformed
        if (rbn_naiveBayse.isSelected()) {
            try {
                Instances trainSet = new Instances(new BufferedReader(new FileReader(TRAIN_ARFF)));
                Instances testSet = new Instances(new BufferedReader(new FileReader(TEST_ARFF)));
                trainSet.setClassIndex(trainSet.numAttributes() - 1);
                testSet.setClassIndex(testSet.numAttributes() - 1);
                NaiveBayes naiveBayes = new NaiveBayes();
                System.out.print("Training on " + trainSet.size() + " examples... ");
                naiveBayes.buildClassifier(trainSet);
                System.out.println("done.");
                Evaluation eval = new Evaluation(testSet);
                eval.evaluateModel(naiveBayes, testSet);
//                etxt_percision.add(Double.toString(eval.pctCorrect()));

etxt_percision.setText(String.format("%.2f", eval.pctCorrect()));
                System.out.println(eval.pctCorrect());

            } catch (Exception ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Unable to train classifier.");
                System.err.println("\t" + ex.getMessage());
            }
        }
    }//GEN-LAST:event_rbn_naiveBayseActionPerformed

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
    private javax.swing.JTextField etxt_percision;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton rbn_logestic;
    private javax.swing.JRadioButton rbn_naiveBayse;
    private javax.swing.JRadioButton rbtn_unigram;
    private javax.swing.JTextField txt_question;
    private javax.swing.JTextField txt_question1;
    // End of variables declaration//GEN-END:variables
}
