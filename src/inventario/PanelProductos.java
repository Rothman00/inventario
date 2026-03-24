package inventario;

import conexion.consumo;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PanelProductos extends JPanel {

    private JTextField txtNombre, txtPrecio;
    private JComboBox<String> cboUnidad, cboEstado;
    private JButton btnGuardar, btnEliminar, btnLimpiar;
    private JTable tabla;
    private DefaultTableModel modelo;
    private int idActual = 0;
    private final List<Integer> idsUnidades = new ArrayList<>();

    public PanelProductos() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        recargarUnidades();
        cargarDatos();
    }

    // ── Formulario ────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Datos de Producto"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Fila 0
        g.gridy = 0;
        g.gridx = 0; g.weightx = 0;   p.add(new JLabel("Nombre:"), g);
        txtNombre = new JTextField(20);
        g.gridx = 1; g.weightx = 1;   p.add(txtNombre, g);
        g.gridx = 2; g.weightx = 0;   p.add(new JLabel("Precio:"), g);
        txtPrecio = new JTextField(10);
        g.gridx = 3; g.weightx = 0.5; p.add(txtPrecio, g);

        // Fila 1
        g.gridy = 1;
        g.gridx = 0; g.weightx = 0;   p.add(new JLabel("Unidad:"), g);
        cboUnidad = new JComboBox<>();
        g.gridx = 1; g.weightx = 1;   p.add(cboUnidad, g);
        g.gridx = 2; g.weightx = 0;   p.add(new JLabel("Estado:"), g);
        cboEstado = new JComboBox<>(new String[]{"A", "I"});
        g.gridx = 3; g.weightx = 0;   p.add(cboEstado, g);

        // Fila 2 – Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        btnGuardar  = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar  = new JButton("Limpiar");
        btnEliminar.setEnabled(false);
        btns.add(btnGuardar); btns.add(btnEliminar); btns.add(btnLimpiar);
        g.gridy = 2; g.gridx = 0; g.gridwidth = 4; g.weightx = 1;
        p.add(btns, g);

        btnGuardar .addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar .addActionListener(e -> limpiar());
        return p;
    }

    // ── Tabla ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        modelo = new DefaultTableModel(
                new String[]{"ID", "Nombre", "Precio", "Unidad", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setMaxWidth(55);
        tabla.getColumnModel().getColumn(4).setMaxWidth(65);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) seleccionar();
        });
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createTitledBorder("Listado de Productos"));
        return sp;
    }

    // ── Lógica ────────────────────────────────────────────────────────────────
    public void recargarUnidades() {
        cboUnidad.removeAllItems();
        idsUnidades.clear();
        consumo.query(
            "SELECT uni_id, uni_nombre FROM inv_unidad WHERE uni_estado='A' ORDER BY uni_nombre"
        ).forEach(r -> {
            idsUnidades.add(Integer.parseInt(r.get("uni_id").toString()));
            cboUnidad.addItem(r.get("uni_nombre").toString());
        });
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        consumo.query(
            "SELECT p.pro_id, p.pro_nombre, p.pro_precio, u.uni_nombre, p.pro_estado " +
            "FROM inv_producto p " +
            "LEFT JOIN inv_unidad u ON p.pro_unidad = u.uni_id " +
            "ORDER BY p.pro_id"
        ).forEach(r -> modelo.addRow(new Object[]{
            r.get("pro_id"), r.get("pro_nombre"), r.get("pro_precio"),
            r.get("uni_nombre"), r.get("pro_estado")
        }));
    }

    private void seleccionar() {
        int row = tabla.getSelectedRow();
        idActual = Integer.parseInt(modelo.getValueAt(row, 0).toString());
        txtNombre.setText(modelo.getValueAt(row, 1).toString());
        txtPrecio.setText(modelo.getValueAt(row, 2).toString());
        // Buscar unidad del producto para seleccionar en combo
        List<Map<String, Object>> res = consumo.query(
            "SELECT pro_unidad FROM inv_producto WHERE pro_id=?", idActual);
        if (!res.isEmpty() && res.get(0).get("pro_unidad") != null) {
            int uid = Integer.parseInt(res.get(0).get("pro_unidad").toString());
            int idx = idsUnidades.indexOf(uid);
            if (idx >= 0) cboUnidad.setSelectedIndex(idx);
        }
        cboEstado.setSelectedItem(modelo.getValueAt(row, 4).toString());
        btnEliminar.setEnabled(true);
        btnGuardar.setText("Actualizar");
    }

    private void guardar() {
        String nom  = txtNombre.getText().trim();
        String prec = txtPrecio.getText().trim();
        String est  = cboEstado.getSelectedItem().toString();
        if (nom.isEmpty() || prec.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Complete nombre y precio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idsUnidades.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero cree al menos una unidad activa.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double precio = Double.parseDouble(prec);
            int uniId = idsUnidades.get(cboUnidad.getSelectedIndex());
            if (idActual == 0) {
                consumo.execute(
                    "INSERT INTO inv_producto(pro_nombre, pro_precio, pro_unidad, pro_estado) VALUES(?,?,?,?)",
                    nom, precio, uniId, est);
                JOptionPane.showMessageDialog(this, "Producto registrado correctamente.");
            } else {
                consumo.execute(
                    "UPDATE inv_producto SET pro_nombre=?, pro_precio=?, pro_unidad=?, pro_estado=? WHERE pro_id=?",
                    nom, precio, uniId, est, idActual);
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            }
            limpiar(); cargarDatos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "El precio debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (idActual == 0) return;
        if (JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar este producto?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            consumo.execute("DELETE FROM inv_producto WHERE pro_id=?", idActual);
            JOptionPane.showMessageDialog(this, "Producto eliminado.");
            limpiar(); cargarDatos();
        }
    }

    private void limpiar() {
        idActual = 0;
        txtNombre.setText(""); txtPrecio.setText("");
        if (!idsUnidades.isEmpty()) cboUnidad.setSelectedIndex(0);
        cboEstado.setSelectedIndex(0);
        btnEliminar.setEnabled(false);
        btnGuardar.setText("Guardar");
        tabla.clearSelection();
    }
}
