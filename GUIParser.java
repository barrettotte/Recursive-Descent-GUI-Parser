/*
File: GUIParser.java
Author: Barrett Otte
Date: 06-15-2017
Summary: This code takes a text file and attempts to parse it to a GUI based on
        the grammar language listed below.
*/
/*  GUI Grammar Language Provided:
    gui ::=
        Window STRING '(' NUMBER ',' NUMBER ')' layout widgets End '.'
    layout ::=
        Layout layout_type ':'
    layout_type ::=
        Flow |
        Grid '(' NUMBER ',' NUMBER [',' NUMBER ',' NUMBER] ')'
    widgets ::=
        widget widgets |
        widget
    widget ::=
        Button STRING ';' |
        Group radio_buttons End ';' |
        Label STRING ';' |
        Panel layout widgets End ';' |
        Textfield NUMBER ';'
    radio_buttons ::=
        radio_button radio_buttons |
        radio_button
    radio_button ::=
        Radio STRING ';'
*/

package botteproject1v2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;


public class GUIParser {
    
    private final String DELIMITERREGEX = "(?<=[(),\".;:])|(?=[(),\".;:])| "; //Performs regex search
    private ArrayList<String> tokenList;
    private BufferedReader buffReader;
    private JFrame mainFrame;
    private int currentIndex;
    private boolean isFrame;
    private JPanel panel;
    private String widgetText;
    private ButtonGroup radioGroup;
    private JRadioButton radioButton;
    private JTextField textField;
    
    
    /**
     * Constructor begins the parsing process.
     */
    public GUIParser() {
        tokenList = new ArrayList<>();
        currentIndex = 0;
        retrieveFile();
    }


    /**
     * Uses the JFileChooser to select a text file specified by the user.
     * The text file will then be analyzed and prepared for parsing.
     */
    private void retrieveFile() {
        try {
            int option = -1;
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt", "text"));
            chooser.setDialogTitle("Recursive Descent GUI Parser");
            
            while (option != JFileChooser.APPROVE_OPTION && option != JFileChooser.CANCEL_OPTION) {
                option = chooser.showOpenDialog(null);
            }
            if (option == JFileChooser.CANCEL_OPTION) {
                return;
            }
            buffReader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
            System.out.println("Loaded: " + chooser.getSelectedFile().getName() + "\n\n");
            analyzeInput();
            parseGUI();
        } 
        catch (IOException e){
            System.out.println("Problem occurred while reading selected file.");
        }
    }

    
    /**
     * Creates the ArrayList that holds all of the tokens of the selected text file.
     * It uses a regular expression defined above to break up each line of the text 
     * file into tokens.
     */
    private void analyzeInput() {
        try{       
            String line;
            boolean isInQuotes = false;
            String text = "";
            
            while ((line = buffReader.readLine()) != null) {
                line = line.trim();
                String[] split = line.split(DELIMITERREGEX);
                System.out.println("Input Line: " + line);

                for(int i = 0; i < split.length; i++){ 
                    split[i] = split[i].trim();
                    
                    if(isInQuotes){
                        if(split[i].equals("\"")){
                            tokenList.add(text.trim());
                            System.out.println("\tToken: " + text.trim());
                            text = "";
                            tokenList.add(split[i]);
                            System.out.println("\tToken: " + split[i]);
                        }
                        else{
                            text += split[i] + " ";  
                        }
                    }  
                    else if(split[i].trim().length() > 0){
                        tokenList.add(split[i].trim());
                        System.out.println("\tToken: " + split[i]);
                    }  
                    if(split[i].equals("\"")){
                        isInQuotes = !isInQuotes;
                    }             
                }
            }
        }
        catch(IOException e){
            System.out.println("A problem occurred with the selected file's content.");
            return;
        }
        System.out.println("--------------------- Token Splitting Done. -----------------------");
    }
    
    
    /**
     * This is where the gui is parsed using recursive descent. It takes the tokenList
     * token by token and evaluates it based on the specified grammar language. 
     * When an error occurs, it stops where it is and prints a specified message
     * having to do with the error.
     * NOTE: This function is rather unreadable and could use some work.
     */
    private void parseGUI() {
        int width, height;

        //Start JFrame Creation:
        if (tokenList.get(currentIndex).equalsIgnoreCase("Window")) {
            isFrame = true;
            mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            System.out.println("***Created JFrame***");
            currentIndex++;

            //Start String:
            if (tokenList.get(currentIndex).equals("\"")) {
                currentIndex++;
                mainFrame.setTitle(tokenList.get(currentIndex));
                System.out.println("***Added title to JFrame***");
                currentIndex++;

                //End String
                if (tokenList.get(currentIndex).equals("\"")) {
                    currentIndex++;

                    //Start Parsing JFrame parameters:
                    if (tokenList.get(currentIndex).equals("(")) {
                        currentIndex++;
                        try {
                            width = Integer.parseInt(tokenList.get(currentIndex));
                            System.out.println("***Successfully Parsed Width***");
                        } catch (NumberFormatException e) {
                            System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid window width.");
                            return;
                        }
                        currentIndex++;
                        if (tokenList.get(currentIndex).equals(",")) {
                            currentIndex++;
                            try {
                                height = Integer.parseInt(tokenList.get(currentIndex));
                                System.out.println("***Successfully Parsed Height***");
                            } catch (NumberFormatException e) {
                                System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid window height.");
                                return;
                            }
                            currentIndex++;

                            //End JFrame Parameters
                            if (tokenList.get(currentIndex).equals(")")) {
                                mainFrame.setSize(width, height);
                                System.out.println("***Set Size of JFrame***");
                                currentIndex++;
                                if (parseLayout()) {
                                    System.out.println("--------------------- JFrame Layout Done. -----------------------");
                                    if (parseWidgets()) {
                                        System.out.println("--------------------- JFrame Widgets Done. -----------------------");
                                        if (tokenList.get(currentIndex).equalsIgnoreCase("End")) {
                                            currentIndex++;
                                            if (tokenList.get(currentIndex).equals(".")) {
                                                System.out.println("***Setting JFrame to visible***");
                                                System.out.println("--------------------- GUI Parsing Done. -----------------------");
                                                mainFrame.setVisible(true);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Problem with GUI syntax detected. GUI was unable to be parsed.");
    }
    
    
    /**
     * Takes care of parsing the layout. This is a boolean to tell whether the
     * layout was correctly parsed or not.
     * @return 
     */
    private boolean parseLayout(){
        if(tokenList.get(currentIndex).equalsIgnoreCase("Layout")){
            currentIndex++;
            if(parseLayoutType()){
                if(tokenList.get(currentIndex).equals(":")){
                    currentIndex++;
                    System.out.println("***Successfully Parsed Layout***");
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * Parses the layout based on its type (flow/grid) and its parameters.
     * @return 
     */
    private boolean parseLayoutType(){
        int rows, cols, colSpacing, rowSpacing;
        
        if(tokenList.get(currentIndex).equalsIgnoreCase("Flow")){
            if(isFrame){
                mainFrame.setLayout(new FlowLayout());
            }
            else{
                panel.setLayout(new FlowLayout());
            }
            currentIndex++;
            System.out.println("***Successfully Parsed a Flow Layout***");
            return true;
        }
        else if(tokenList.get(currentIndex).equalsIgnoreCase("Grid")){
            currentIndex++;
            
            //Start parsing Grid Layout parameters:
            if(tokenList.get(currentIndex).equals("(")){
                currentIndex++;
                try{
                    rows = Integer.parseInt(tokenList.get(currentIndex));
                    System.out.println("***Successfully parsed Grid Layout Rows***");
                }
                catch(NumberFormatException e){
                    System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid Grid Layout Row Value.");
                    return false;
                }
                currentIndex++;
                if(tokenList.get(currentIndex).equals(",")){
                    currentIndex++;
                    try{
                        cols = Integer.parseInt(tokenList.get(currentIndex));
                        System.out.println("***Successfully parsed Grid Layout Columns***");
                    }
                    catch(NumberFormatException e){
                        System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid Grid Layout Column Value.");
                        return false;
                    }
                    currentIndex++;
                    
                    //End parameter parsing if only two parameters are given:
                    if(tokenList.get(currentIndex).equals(")")){
                        if(isFrame){
                            mainFrame.setLayout(new GridLayout(rows,cols));   
                        }
                        else{
                            panel.setLayout(new GridLayout(rows,cols));
                        }
                        System.out.println("***Successfully parsed GridLayout(rows,cols)***");
                        currentIndex++;
                        return true;
                    }
                    
                    //Continue parameter parsing for Grid Layout if more are given:
                    else if(tokenList.get(currentIndex).equals(",")){
                        currentIndex++;
                        try{
                            colSpacing = Integer.parseInt(tokenList.get(currentIndex));
                            System.out.println("***Successfully parsed Grid Layout Column Spacing***");
                        }
                        catch(NumberFormatException e){
                            System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid Grid Layout Column Spacing Value.");
                            return false;
                        }
                        currentIndex++;
                        if(tokenList.get(currentIndex).equals(",")){
                            currentIndex++;
                            try{
                                rowSpacing = Integer.parseInt(tokenList.get(currentIndex));
                                System.out.println("***Successfully parsed Grid Layout Row Spacing***");
                            }
                            catch(NumberFormatException e){
                                System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid Grid Layout Row Spacing Value.");
                                return false;
                            }
                            currentIndex++;
                            if(tokenList.get(currentIndex).equals(")")){
                                if(isFrame){
                                    mainFrame.setLayout(new GridLayout(rows, cols, colSpacing, rowSpacing));
                                }
                                else{
                                    panel.setLayout(new GridLayout(rows, cols, colSpacing, rowSpacing));
                                }
                                currentIndex++;
                                System.out.println("***Successfully parsed GridLayout(rows,cols,colSpacing,rowSpacing)***");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Problem with Layout syntax detected. Layout was unable to be parsed.");
        return false;
    }
    
    
    /**
     * recursive function to handle parsing all widgets.
     * returns false when all widgets have been parsed or one is unable to be parsed.
     * @return 
     */
    private boolean parseWidgets(){
        if(parseWidget()){
            if(parseWidgets()){
                return true;
            }
            return true;
        }
        return false;
    }
    
    
    /**
     * Parses each widget depending on its type and the grammar language.
     * Returns false if the widget cannot be parsed.
     * @return 
     */
    private boolean parseWidget(){
        
        if(tokenList.get(currentIndex).equalsIgnoreCase("Button")){
            currentIndex++;
            if(tokenList.get(currentIndex).equals("\"")){
                currentIndex++;
                widgetText = tokenList.get(currentIndex);
                currentIndex++;
                if(tokenList.get(currentIndex).equals("\"")){
                    currentIndex++;
                    if(tokenList.get(currentIndex).equals(";")){
                        if(isFrame){
                            JButton button = new JButton(widgetText);
                            button.addActionListener((ActionEvent e) -> {
                                textField.setText(button.getText());
                                System.out.println("Pressed " + button.getText() + " Button.");
                            });
                            mainFrame.add(button);
                        }
                        else{
                            JButton button = new JButton(widgetText);
                            button.addActionListener((ActionEvent e) -> {
                                textField.setText(button.getText());
                                System.out.println("Pressed " + button.getText() + " Button.");
                            });
                            panel.add(button);
                        }
                        currentIndex++;
                        System.out.println("***Successfully parsed Button " + widgetText + " ***");
                        return true;
                    }
                }
            }
        }
        
        else if(tokenList.get(currentIndex).equalsIgnoreCase("Group")){
            radioGroup = new ButtonGroup();
            currentIndex++;
            if(parseRadioButtons()){
                if(tokenList.get(currentIndex).equalsIgnoreCase("End")){
                    currentIndex++;
                    if(tokenList.get(currentIndex).equals(";")){
                        currentIndex++;
                        System.out.println("***Successfully parsed Group***");
                        return true;
                    }
                }
            }
        }
        
        else if(tokenList.get(currentIndex).equalsIgnoreCase("Label")){
            currentIndex++;
            if(tokenList.get(currentIndex).equals("\"")){
                currentIndex++;
                widgetText = tokenList.get(currentIndex);
                currentIndex++;
                if(tokenList.get(currentIndex).equals("\"")){
                    currentIndex++;
                    if(tokenList.get(currentIndex).equals(";")){
                        if(isFrame){
                            mainFrame.add(new JLabel(widgetText));
                        }
                        else{
                            panel.add(new JLabel(widgetText));
                        }
                        currentIndex++;
                        System.out.println("***Successfully parsed Label " + widgetText + " ***");
                        return true;
                    }
                }
            }
        }
        
        else if(tokenList.get(currentIndex).equalsIgnoreCase("Panel")){
            if(isFrame){
                mainFrame.add(panel = new JPanel());
            }
            else{
                panel.add(panel = new JPanel());
            }
            isFrame = false;
            currentIndex++;
            if(parseLayout()){
                System.out.println("--------------------- Panel Layout Done. -----------------------");
                if(parseWidgets()){
                    System.out.println("--------------------- Panel Widgets Done. -----------------------");
                    if(tokenList.get(currentIndex).equalsIgnoreCase("End")){
                        currentIndex++;
                        if (tokenList.get(currentIndex).equals(";")) {
                            currentIndex++;
                            System.out.println("***Successfully parsed panel***");
                            return true;
                        }
                    }
                }
            }
        }
        
        else if(tokenList.get(currentIndex).equalsIgnoreCase("Textfield")){
            int length;
            currentIndex++;
            try{
                length = Integer.parseInt(tokenList.get(currentIndex));
                System.out.println("***Successfully parsed Textfield Length***");
            }
            catch(NumberFormatException e){
                System.out.println("Error with syntax. " + tokenList.get(currentIndex) + " is an invalid Textfield Length.");
                return false;
            }
            currentIndex++;
            if(tokenList.get(currentIndex).equals(";")){
                if(isFrame){
                    mainFrame.add(textField = new JTextField(length));
                }
                else{
                    panel.add(textField = new JTextField(length));
                }
                currentIndex++;
                System.out.println("***Successfully parsed T-extfield***");
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Recursive function to parse all radio buttons.
     * Returns false when all radio buttons are parsed or when one is unable to
     * be parsed.
     * @return 
     */
    private boolean parseRadioButtons(){
        if(parseRadioButton()){
            if(parseRadioButtons()){
                return true;
            }
            return true;
        }
        return false;
    }
     
    
    /**
     * Handles parsing the radio button.
     * Returns false if the  radio button was unable to be parsed.
     * @return 
     */
    private boolean parseRadioButton(){
        if (tokenList.get(currentIndex).equalsIgnoreCase("Radio")) {
            currentIndex++;
            if (tokenList.get(currentIndex).equals("\"")) {
                currentIndex++;
                widgetText = tokenList.get(currentIndex);
                currentIndex++;
                if (tokenList.get(currentIndex).equals("\"")) {
                    currentIndex++;
                    if (tokenList.get(currentIndex).equals(";")) {
                        radioButton = new JRadioButton(widgetText);
                        radioGroup.add(radioButton);
                        if (isFrame) {
                            mainFrame.add(radioButton);
                        } 
                        else {
                            panel.add(radioButton);
                        }
                        currentIndex++;
                        System.out.println("***Successfully parsed Radio Button " + widgetText + " ***");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}