package inventario;

import java.awt.*;
import javax.swing.*;

public class inventario extends JFrame {

    private final PanelProductos panelProductos;
    private final PanelKardex   panelKardex;

    public inventario() {
        setTitle("Sistema de Inventario");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 640));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Unidades",    new PanelUnidades());

        panelProductos = new PanelProductos();
        tabs.addTab("Productos",   panelProductos);

        panelKardex = new PanelKardex();
        tabs.addTab("Movimientos", panelKardex);

        // Refrescar combos al cambiar de pestaña
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) panelProductos.recargarUnidades();
            else if (idx == 2) panelKardex.recargarProductos();
        });

        add(tabs);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        EventQueue.invokeLater(() -> new inventario().setVisible(true));
    }
}
