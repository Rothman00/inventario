package inventario;

import conexion.consumo;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PanelUnidades extends JPanel {

    private JTextField txtNombre, txtAbreviatura;
    private JComboBox<String> cboEstado;
    private JButton btnGuardar, btnEliminar, btnLimpiar;
    private JTable tabla;
    private DefaultTableModel modelo;
    private int idActual = 0;

    public PanelUnidades() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        cargarDatos();
    }

    // ── Formulario ────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Datos de Unidad"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Fila 0
        g.gridy = 0;
        g.gridx = 0; g.weightx = 0;  p.add(new JLabel("Nombre:"), g);
        txtNombre = new JTextField(20);
        g.gridx = 1; g.weightx = 1;  p.add(txtNombre, g);
        g.gridx = 2; g.weightx = 0;  p.add(new JLabel("Abreviatura:"), g);
        txtAbreviatura = new JTextField(8);
        g.gridx = 3; g.weightx = 0.5; p.add(txtAbreviatura, g);
        g.gridx = 4; g.weightx = 0;  p.add(new JLabel("Estado:"), g);
        cboEstado = new JComboBox<>(new String[]{"V", "N"});
        g.gridx = 5; g.weightx = 0;  p.add(cboEstado, g);

        // Fila 1 – Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        btnGuardar  = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar  = new JButton("Limpiar");
        btnEliminar.setEnabled(false);
        btns.add(btnGuardar); btns.add(btnEliminar); btns.add(btnLimpiar);
        g.gridy = 1; g.gridx = 0; g.gridwidth = 6; g.weightx = 1;
        p.add(btns, g);

        btnGuardar .addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar .addActionListener(e -> limpiar());
        return p;
    }

    // ── Tabla ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        modelo = new DefaultTableModel(
                new String[]{"ID", "Nombre", "Abreviatura", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setMaxWidth(55);
        tabla.getColumnModel().getColumn(2).setMaxWidth(110);
        tabla.getColumnModel().getColumn(3).setMaxWidth(65);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) seleccionar();
        });
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createTitledBorder("Listado de Unidades"));
        return sp;
    }

    // ── Lógica ────────────────────────────────────────────────────────────────
    private void seleccionar() {
        int row = tabla.getSelectedRow();
        idActual = Integer.parseInt(modelo.getValueAt(row, 0).toString());
        txtNombre.setText(modelo.getValueAt(row, 1).toString());
        txtAbreviatura.setText(modelo.getValueAt(row, 2).toString());
        cboEstado.setSelectedItem(modelo.getValueAt(row, 3).toString());
        btnEliminar.setEnabled(true);
        btnGuardar.setText("Actualizar");
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        consumo.query(
            "SELECT uni_id, uni_nombre, uni_abreviatura, uni_estado " +
            "FROM inv_unidad ORDER BY uni_id"
        ).forEach(r -> modelo.addRow(new Object[]{
            r.get("uni_id"), r.get("uni_nombre"),
            r.get("uni_abreviatura"), r.get("uni_estado")
        }));
    }

    private void guardar() {
        String nom = txtNombre.getText().trim();
        String abr = txtAbreviatura.getText().trim();
        String est = cboEstado.getSelectedItem().toString();
        if (nom.isEmpty() || abr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Complete nombre y abreviatura.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idActual == 0) {
            consumo.execute(
                "INSERT INTO inv_unidad(uni_nombre, uni_abreviatura, uni_estado) VALUES(?,?,?)",
                nom, abr, est);
            JOptionPane.showMessageDialog(this, "Unidad registrada correctamente.");
        } else {
            consumo.execute(
                "UPDATE inv_unidad SET uni_nombre=?, uni_abreviatura=?, uni_estado=? WHERE uni_id=?",
                nom, abr, est, idActual);
            JOptionPane.showMessageDialog(this, "Unidad actualizada correctamente.");
        }
        limpiar(); cargarDatos();
    }

    private void eliminar() {
        if (idActual == 0) return;
        if (JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar esta unidad?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            consumo.execute("DELETE FROM inv_unidad WHERE uni_id=?", idActual);
            JOptionPane.showMessageDialog(this, "Unidad eliminada.");
            limpiar(); cargarDatos();
        }
    }

    private void limpiar() {
        idActual = 0;
        txtNombre.setText(""); txtAbreviatura.setText("");
        cboEstado.setSelectedIndex(0);
        btnEliminar.setEnabled(false);
        btnGuardar.setText("Guardar");
        tabla.clearSelection();
    }
}
