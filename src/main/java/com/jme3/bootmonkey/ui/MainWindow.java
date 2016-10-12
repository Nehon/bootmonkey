package com.jme3.bootmonkey.ui;

import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Nehon on 12/10/2016.
 */
public class MainWindow {

    public static void main(String... argv) throws Exception{
        new MainWindow();
    }

    public MainWindow() throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JFrame mainFrame = new JFrame("Boot monkey");
                mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                //Add the ubiquitous "Hello World" label.
                JLabel label = new JLabel("Create a new jMonkeyEngine3 project");
                mainFrame.getContentPane().add(label);

                mainFrame.setLocationRelativeTo(null);
                mainFrame.pack();
                mainFrame.setVisible(true);

                try {
                    mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icons/icon.png")));
                    SystemTray.getSystemTray().add(new TrayIcon(Toolkit.getDefaultToolkit().getImage("/icons/icon.png")));
                } catch (AWTException e) {
                    e.printStackTrace();
                }
//                mainFrame.addComponentListener(new ComponentAdapter() {
//                    @Override
//                    public void componentResized(ComponentEvent e) {
//                        prefs.putInt(EDITOR_WIDTH, e.getComponent().getWidth());
//                        prefs.putInt(EDITOR_HEIGHT, e.getComponent().getHeight());
//                        System.err.println("size : "+ e.getComponent().getWidth() + " " + e.getComponent().getHeight());
//                    }
//
//                    @Override
//                    public void componentMoved(ComponentEvent e) {
//                        prefs.putInt(EDITOR_X, e.getComponent().getX());
//                        prefs.putInt(EDITOR_Y, e.getComponent().getY());
//                        System.err.println(e.getComponent().getX() + " " + e.getComponent().getY());
//                    }
//                });
//
//
//
//                mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//                mainFrame.addWindowListener(new WindowAdapter() {
//                    @Override
//                    public void windowClosed(WindowEvent e) {
//                        stop();
//                    }
//
//
//                });
//
//                mainFrame.setJMenuBar(createMainMenu());
//
//                JPanel centerPane = new JPanel();
//                mainFrame.getContentPane().add(centerPane, BorderLayout.CENTER);
//
//                centerPane.setLayout(new BorderLayout());
//
//
//                // Register the SwingGui layer and let it handle all of the requests
//                // for which it is capable.
//                SwingGui gui = spix.registerService(SwingGui.class, new SwingGui(spix, mainFrame));
//
//                SceneExplorerPanel sceneExplorerPanel = new SceneExplorerPanel(DockPanel.Slot.West, centerPane, gui);
//                sceneExplorerPanel.setPreferredSize(new Dimension(250,10));
//                sceneExplorerPanel.unDock();
//
//                PropPanel propertiesPanel = new PropPanel(centerPane);
//                propertiesPanel.setPreferredSize(new Dimension(250,10));
//                propertiesPanel.unDock();
//
//                JToolBar eastToolBar = new JToolBar(JToolBar.VERTICAL);
//                eastToolBar.setFloatable(false);
//                mainFrame.getContentPane().add(eastToolBar, BorderLayout.EAST);
//                NoneSelectedButtonGroup groupE = new NoneSelectedButtonGroup();
//                groupE.add( propertiesPanel.getButton());
//                eastToolBar.add( propertiesPanel.getButton());
//
//                JToolBar westToolBar = new JToolBar(JToolBar.VERTICAL);
//                westToolBar.setFloatable(false);
//                mainFrame.getContentPane().add(westToolBar, BorderLayout.WEST);
//                NoneSelectedButtonGroup groupW = new NoneSelectedButtonGroup();
//                groupW.add( sceneExplorerPanel.getButton());
//                westToolBar.add( sceneExplorerPanel.getButton());
//
//
//                // Register a custom read-only display for Vector3fs that formats the values
//                // a little better.
//                gui.registerComponentFactory(Vector3f.class,
//                        new DefaultComponentFactory(new Vec3fStringFunction()));
//                gui.registerComponentFactory(SwingGui.EDIT_CONTEXT, Vector3f.class,
//                        new DefaultComponentFactory(Vector3fPanel.class));
//                gui.registerComponentFactory(SwingGui.EDIT_CONTEXT, Quaternion.class,
//                        new DefaultComponentFactory(QuaternionPanel.class));
//
//                PropertyEditorPanel objectEditor = new PropertyEditorPanel(gui, "ui.editor");
//                objectEditor.setPreferredSize(new Dimension(250, 100));
//                propertiesPanel.setComponent(new JScrollPane(objectEditor));
//
//                stateManager.attach(new AwtPanelState(centerPane, BorderLayout.CENTER));
//
//
//
//                // Setup a selection test to change the test label
////                spix.getBlackboard().bind("main.selection.singleSelect",
////                                           testLabel, "text", ToStringFunction.INSTANCE);
//
//                // Bind the selection to the editor panel, converting objects to
//                // property set wrappers if appropriate.
//                spix.getBlackboard().bind("main.selection.singleSelect",
//                        objectEditor, "object",
//                        new ToPropertySetFunction(spix));
//
//                /*spix.getBlackboard().bind("main.selection.singleSelect",
//                                           testLabel2, "text",
//                                           Functions.compose(
//                                                ToStringFunction.INSTANCE,
//                                                new ToPropertySetFunction(spix)));*/
//
//                spix.getBlackboard().get("main.selection", SelectionModel.class).add("Test Selection");
//
//
////                final MatDefEditorWindow matDefEditorWindow = new MatDefEditorWindow(gui);
////                matDefEditorWindow.setVisible(true);
////                mainFrame.addWindowListener(new WindowAdapter() {
////                    @Override
////                    public void windowClosing(WindowEvent e) {
////                        matDefEditorWindow.dispose();
////                    }
////                });
////
////                spix.registerService(MaterialService.class, new MaterialService(stateManager.getState(MaterialAppState.class), gui));
            }
        });
    }
}
