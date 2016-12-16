package com.jme3.bootmonkey.ui;

import com.jme3.bootmonkey.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import static com.jme3.bootmonkey.ui.SpringLayoutHelper.makeCompactGrid;

/**
 * Created by Nehon on 12/10/2016.
 */
public class MainWindow {

    public static final String REPOSITORIES = "bootmonkey.repositories";
    public static final String LAST_SELECTED_REPOSITORY = "bootmonkey.lastSelectedRepository";
    public static final String DEFAULT_REPOSITORY = "https://github.com/Nehon/base-jme.git";
    public static final String LAST_DIRECTORY = "bootmonkey.lastDirectory";
    private ProgressDialog progressDialog;
    private Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
    private String repoList;

    private JComboBox<String> repoField;
    private JComboBox<String> versionField;

    public static void main(String... argv) throws Exception{
        new MainWindow();
    }

    public MainWindow() throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                //creating the main window
                final JFrame mainFrame = new JFrame("Boot monkey");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.getContentPane().setLayout(new BorderLayout());
                mainFrame.setResizable(false);

                //setting up windows icons
                setupIcons(mainFrame);

                //creating the progress Dialog
                progressDialog = new ProgressDialog(mainFrame, "Generation progress");

                //presentation label
                JLabel label = new JLabel("Create a new jMonkeyEngine3 project");
                mainFrame.getContentPane().add(label, BorderLayout.NORTH);

                //creating the form
                JPanel container = new JPanel(new SpringLayout());

                //Project name field
                JLabel l = new JLabel("Project Name: ", JLabel.TRAILING);
                container.add(l);
                final JTextField projectNameField = new JTextField(10);
                l.setLabelFor(projectNameField);
                projectNameField.setText("MyGame");
                container.add(projectNameField);

                //Default package field
                l = new JLabel("Default package: ", JLabel.TRAILING);
                container.add(l);
                final JTextField packageField  = new JTextField(10);
                l.setLabelFor(packageField);
                packageField.setText("com.mycompany.mygame");
                container.add(packageField);

                //Template repository url field
                l = new JLabel("Template repo url: ", JLabel.TRAILING);
                container.add(l);
                repoList = prefs.get(REPOSITORIES, DEFAULT_REPOSITORY);
                String lastRepo = prefs.get(LAST_SELECTED_REPOSITORY, DEFAULT_REPOSITORY);
                String [] values = repoList.split("\\|");

                JPanel repoFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

                repoField  = new JComboBox<String>(values);
                repoField.setEditable(true);
                repoField.setSelectedItem(lastRepo);
                l.setLabelFor(repoField);
                repoFieldPanel.add(repoField);
                //creating an info button.
                JButton infoButton = new JButton("?");
                infoButton.setPreferredSize(new Dimension(25, 20));
                repoFieldPanel.add(infoButton);
                container.add(repoFieldPanel);
                infoButton.addActionListener((e) -> {
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI((String)repoField.getSelectedItem()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                //Template version field
                l = new JLabel("Template version: ", JLabel.TRAILING);
                container.add(l);
                versionField  = new JComboBox<String>();
                versionField.setRenderer((JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) -> {
                    JLabel label1 = new JLabel(value);
                    if(value.contains("/")){
                        label1.setText(value.substring(value.lastIndexOf("/") + 1));
                    }
                    return label1;
                });
                container.add(versionField);
                versionField.addItem("SNAPSHOT");
                //fetching data
                new FetchBranchWorker().execute();

                //setting up the callback to fetch data whenever the template repo changes
                repoField.addActionListener((e) -> new FetchBranchWorker().execute());

                //Project folder field.
                l = new JLabel("Create project in folder: ", JLabel.TRAILING);
                container.add(l);
                JPanel browseFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
                final JTextField baseDirField  = new JTextField(20);
                l.setLabelFor(baseDirField);
                String lastDir = prefs.get(LAST_DIRECTORY, "");
                baseDirField.setText(lastDir);
                browseFieldPanel.add(baseDirField);
                //creating a browse button.
                JButton browseButton = new JButton("...");
                browseButton.setPreferredSize(new Dimension(20, 15));
                browseFieldPanel.add(browseButton);
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                browseButton.addActionListener((e) -> {
                    int returnVal = fc.showOpenDialog(mainFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        baseDirField.setText(file.getPath());
                    }
                });
                container.add(browseFieldPanel);

                /*The advanced settings frame
                final JFrame advancedSettingsFrame=new JFrame("Advanced Settings");
                advancedSettingsFrame.getContentPane().setLayout(new BorderLayout());
                advancedSettingsFrame.setResizable(false);*/
                
                /*JButton button = new JButton("Close");
                button.addActionListener((e) -> {
                    advancedSettingsFrame.setVisible(false);
                });
                advancedSettingsFrame.getContentPane().add(button, BorderLayout.SOUTH);*/
                
                /*The advanced settings button
                l = new JLabel("Advanced Settings: ", JLabel.TRAILING);                 
                JButton defineButton = new JButton("Show advanced settings");
                defineButton.addActionListener((e) -> {
                    advancedSettingsFrame.pack();
                    advancedSettingsFrame.setLocationRelativeTo(mainFrame);
                    advancedSettingsFrame.setVisible(true);
                });
                
                JPanel settingsPanel=new JPanel(new FlowLayout(FlowLayout.LEADING));
                settingsPanel.add(defineButton);
                
                container.add(l);
                container.add(settingsPanel);*/
                
                //making the layout.
                
                //final JPanel settingsContainer = new JPanel(new SpringLayout());
                
                l=new JLabel("JME Version code:", JLabel.TRAILING);
                final JTextField jmeVersionField  = new JTextField(10);
                jmeVersionField.setText("[3.1)");
                l.setLabelFor(jmeVersionField);
                
                container.add(l);    
                container.add(jmeVersionField);
                
                //mainFrame.getContentPane().add(settingsContainer, BorderLayout.SOUTH);
                
                /*makeCompactGrid(settingsContainer,
                        1, 2, //rows, cols
                        6, 6,        //initX, initY
                        7, 7);       //xPad, yPad*/
                
                makeCompactGrid(container,
                        6, 2, //rows, cols
                        5, 5,        //initX, initY
                        7, 7);       //xPad, yPad

                mainFrame.getContentPane().add(container, BorderLayout.CENTER);
                
                //The create button
                JButton button = new JButton("Create");
                button.addActionListener((e) -> {

                    if(!validate(mainFrame, projectNameField, packageField, repoField, baseDirField)){
                        return;
                    }
                    progressDialog.display();

                    String repoUrl = (String) repoField.getSelectedItem();
                    String baseDir = baseDirField.getText();

                    savePreferences(repoUrl, baseDir);

                    new ProjectGenerationWorker(projectNameField, packageField, jmeVersionField, repoUrl, baseDir).execute();

                });
                mainFrame.getContentPane().add(button, BorderLayout.SOUTH);

                //pack and display
                mainFrame.pack();
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setVisible(true);
            }


        });
    }

    private void savePreferences(String repoUrl, String baseDir) {
        if(!repoList.contains(repoUrl)){
            repoList += "|" + repoUrl;
            prefs.put(REPOSITORIES, repoList);
        }
        prefs.put(LAST_SELECTED_REPOSITORY, repoUrl);
        prefs.put(LAST_DIRECTORY, baseDir);
    }

    private void setupIcons(JFrame mainFrame) {
        try {
            List<Image> icons = new ArrayList<>();
            icons.add(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icons/iconx16.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icons/iconx32.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icons/iconx64.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icons/iconx128.png")));
            mainFrame.setIconImages(icons);
            SystemTray.getSystemTray().add(new TrayIcon(Toolkit.getDefaultToolkit().getImage("/icons/icon16.png")));
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private boolean validate(JFrame mainFrame, JTextField projectNameField, JTextField packageField, JComboBox<String> repoField, JTextField baseDirField) {
        if(packageField.getText().equals("")){
            JOptionPane.showMessageDialog(mainFrame, "Package field must not be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(projectNameField.getText().equals("")){
            JOptionPane.showMessageDialog(mainFrame, "Project name field must not be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(repoField.getSelectedItem().equals("")){
            JOptionPane.showMessageDialog(mainFrame, "Template repository url must not be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(baseDirField.getText().equals("")){
            JOptionPane.showMessageDialog(mainFrame, "Project folder must not be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private class Step{
        String text;
        java.util.List<String> errors;

        public Step(String text, java.util.List<String> errors) {
            this.text = text;
            this.errors = errors;
        }

        @Override
        public String toString() {
            return "Step{" +
                    "text='" + text + '\'' +
                    ", errors=" + errors +
                    '}';
        }
    }

    private class FetchBranchWorker extends SwingWorker<Void, Step>{
        List<String> list;

        @Override
        protected Void doInBackground() throws Exception {
            try {
                list = GitUtils.getRemoteBranches((String)repoField.getSelectedItem());
            } catch (IOException | GitAPIException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void done() {
            versionField.removeAllItems();
            versionField.addItem("SNAPSHOT");
            list.forEach(versionField::addItem);
        }
    }


    private class ProjectGenerationWorker extends SwingWorker<Void, Step>{

        Map<String, String> params;

        public ProjectGenerationWorker(JTextField projectNameField, JTextField packageField, JTextField jmeVersionField, String repoUrl, String baseDir) {
            params = new HashMap<>();
            params.put("packageName", packageField.getText());
            params.put("jmeVersion", jmeVersionField.getText());
            params.put("baseDir",  baseDir + "/");
            params.put("projectName", projectNameField.getText());
            params.put("templateUrl", repoUrl);
            
            System.out.println(params.get("jmeVersion"));
        }

        @Override
        protected Void doInBackground() throws Exception {
            ProjectGenerator generator = new ProjectGenerator();
            generator.addGenerationListener((text , errors) -> publish(new Step(text, errors)));
            generator.generate(params);
            return null;
        }

        @Override
        protected void process(java.util.List<Step> steps) {
            steps.forEach((s) -> progressDialog.progress(s.text, s.errors));
        }

    };
}
