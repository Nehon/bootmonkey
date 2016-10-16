package com.jme3.bootmonkey.ui;

import com.jme3.bootmonkey.ProjectGenerator;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

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

    public static void main(String... argv) throws Exception{
        new MainWindow();
    }

    public MainWindow() throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                final JFrame mainFrame = new JFrame("Boot monkey");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.getContentPane().setLayout(new BorderLayout());
                mainFrame.setResizable(false);

                JLabel label = new JLabel("Create a new jMonkeyEngine3 project");
                mainFrame.getContentPane().add(label, BorderLayout.NORTH);


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


                JPanel container = new JPanel(new SpringLayout());

                JLabel l = new JLabel("Project Name: ", JLabel.TRAILING);
                container.add(l);
                final JTextField projectNameField = new JTextField(10);
                l.setLabelFor(projectNameField);
                projectNameField.setText("MyGame");
                container.add(projectNameField);

                l = new JLabel("Default package: ", JLabel.TRAILING);
                container.add(l);
                final JTextField packageField  = new JTextField(10);
                l.setLabelFor(packageField);
                packageField.setText("com.mycompany.mygame");
                container.add(packageField);

                l = new JLabel("Template repo url: ", JLabel.TRAILING);
                container.add(l);

                repoList = prefs.get(REPOSITORIES, DEFAULT_REPOSITORY);
                String lastRepo = prefs.get(LAST_SELECTED_REPOSITORY, DEFAULT_REPOSITORY);
                String [] values = repoList.split("\\|");
                final JComboBox<String> repoField  = new JComboBox<String>(values);
                repoField.setEditable(true);
                repoField.setSelectedItem(lastRepo);
                l.setLabelFor(repoField);
                container.add(repoField);

                l = new JLabel("Create project in folder: ", JLabel.TRAILING);
                container.add(l);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
                final JTextField baseDirField  = new JTextField(20);
                l.setLabelFor(baseDirField);
                String lastDir = prefs.get(LAST_DIRECTORY, "");
                baseDirField.setText(lastDir);
                p.add(baseDirField);
                JButton b = new JButton("...");
                b.setPreferredSize(new Dimension(20, 15));
                p.add(b);
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                b.addActionListener((e) -> {
                    int returnVal = fc.showOpenDialog(mainFrame);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        //This is where a real application would open the file.
                        baseDirField.setText(file.getPath());
                    }
                });
                container.add(p);

                makeCompactGrid(container,
                        4, 2, //rows, cols
                        6, 6,        //initX, initY
                        6, 6);       //xPad, yPad


                mainFrame.getContentPane().add(container, BorderLayout.CENTER);



                JButton button = new JButton("Create");
                button.addActionListener((e) -> {

                    if(!validate(mainFrame, projectNameField, packageField, repoField, baseDirField)){
                        return;
                    }

                    progressDialog.display();

                    final Map<String, String> params = new HashMap<>();
                    String repoUrl = (String) repoField.getSelectedItem();
                    String baseDir = baseDirField.getText();
                    params.put("packageName", packageField.getText());
                    params.put("jmeVersion", "[3.1,)");
                    params.put("baseDir",  baseDir + "/");
                    params.put("projectName", projectNameField.getText());
                    params.put("templateUrl", repoUrl);
                    if(!repoList.contains(repoUrl)){
                        repoList += "|" + repoUrl;
                        prefs.put(REPOSITORIES, repoList);
                    }
                    prefs.put(LAST_SELECTED_REPOSITORY, repoUrl);
                    prefs.put(LAST_DIRECTORY, baseDir);

                    SwingWorker worker = new SwingWorker<Void, Step>(){

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

                    worker.execute();

                });

                mainFrame.getContentPane().add(button, BorderLayout.SOUTH);

                mainFrame.pack();
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setVisible(true);


                progressDialog = new ProgressDialog(mainFrame, "Generation progress");

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
        });
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, cols).
                                getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, cols).
                                getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    /* Used by makeCompactGrid. */
    private SpringLayout.Constraints getConstraintsForCell(
            int row, int col,
            Container parent,
            int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
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
}
