package inventario;

import conexion.consumo;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PanelKardex extends JPanel {

    private JComboBox<String> cboProducto;
    private JLabel lblStock;
    private JRadioButton rbCompra, rbVenta;
    private JTextField txtCantidad;
    private JButton btnRegistrar, btnLimpiar;
    private JTable tabla;
    private DefaultTableModel modelo;
    private final List<Integer> idsProductos = new ArrayList<>();

    public PanelKardex() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildForm(),  BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        recargarProductos();
        cargarHistorial();
    }

    // ── Formulario ────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Registrar Movimiento de Inventario"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 9, 7, 9);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Fila 0 – Producto / Stock
        g.gridy = 0;
        g.gridx = 0; g.weightx = 0;              p.add(new JLabel("Producto:"), g);
        cboProducto = new JComboBox<>();
        g.gridx = 1; g.weightx = 1; g.gridwidth = 3; p.add(cboProducto, g);
        g.gridwidth = 1;
        g.gridx = 4; g.weightx = 0;              p.add(new JLabel("Stock actual:"), g);
        lblStock = new JLabel("0.00");
        lblStock.setFont(lblStock.getFont().deriveFont(Font.BOLD, 15f));
        lblStock.setForeground(new Color(0, 110, 0));
        g.gridx = 5; p.add(lblStock, g);

        // Fila 1 – Tipo movimiento
        g.gridy = 1; g.gridwidth = 1;
        g.gridx = 0; g.weightx = 0; p.add(new JLabel("Tipo:"), g);
        rbCompra = new JRadioButton("Compra / Ingreso", true);
        rbVenta  = new JRadioButton("Venta / Egreso");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbCompra); bg.add(rbVenta);
        JPanel pTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        pTipo.add(rbCompra); pTipo.add(rbVenta);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 5; p.add(pTipo, g);

        // Fila 2 – Cantidad
        g.gridy = 2; g.gridwidth = 1;
        g.gridx = 0; g.weightx = 0; p.add(new JLabel("Cantidad:"), g);
        txtCantidad = new JTextField(12);
        g.gridx = 1; g.weightx = 0; p.add(txtCantidad, g);

        // Fila 3 – Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        btnRegistrar = new JButton("Registrar Movimiento");
        btnLimpiar   = new JButton("Limpiar");
        btns.add(btnRegistrar); btns.add(btnLimpiar);
        g.gridy = 3; g.gridx = 0; g.gridwidth = 6; g.weightx = 1;
        p.add(btns, g);

        cboProducto .addActionListener(e -> actualizarStock());
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar  .addActionListener(e -> limpiar());
        return p;
    }

    // ── Tabla ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        modelo = new DefaultTableModel(
                new String[]{"ID", "Producto", "Fecha", "Stock Ant.", "Ingreso", "Egreso", "Stock Final", "Tipo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(7).setMaxWidth(80);
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createTitledBorder("Historial de Movimientos"));
        return sp;
    }

    // ── Lógica ────────────────────────────────────────────────────────────────
    public void recargarProductos() {
        int prevIdx = cboProducto.getSelectedIndex();
        cboProducto.removeAllItems();
        idsProductos.clear();
        consumo.query(
            "SELECT pro_id, pro_nombre FROM inv_producto WHERE pro_estado='A' ORDER BY pro_nombre"
        ).forEach(r -> {
            idsProductos.add(Integer.parseInt(r.get("pro_id").toString()));
            cboProducto.addItem(r.get("pro_nombre").toString());
        });
        if (prevIdx >= 0 && prevIdx < cboProducto.getItemCount())
            cboProducto.setSelectedIndex(prevIdx);
        actualizarStock();
    }

    /** Obtiene el stock actual (último kar_total) del producto, o 0 si no hay registros. */
    private double getStock(int productoId) {
        List<Map<String, Object>> res = consumo.query(
            "SELECT kar_total FROM inv_kardex WHERE kar_producto=? ORDER BY kar_id DESC LIMIT 1",
            productoId);
        if (!res.isEmpty() && res.get(0).get("kar_total") != null)
            return Double.parseDouble(res.get(0).get("kar_total").toString());
        return 0.0;
    }

    private void actualizarStock() {
        if (cboProducto.getSelectedIndex() < 0 || idsProductos.isEmpty()) {
            lblStock.setText("0.00");
            lblStock.setForeground(Color.RED);
            return;
        }
        double stock = getStock(idsProductos.get(cboProducto.getSelectedIndex()));
        lblStock.setText(String.format("%.2f", stock));
        lblStock.setForeground(stock <= 0 ? Color.RED : new Color(0, 110, 0));
    }

    private void registrar() {
        // Validar selección de producto
        if (cboProducto.getSelectedIndex() < 0 || idsProductos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar cantidad
        String cantStr = txtCantidad.getText().trim();
        if (cantStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese la cantidad.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double cantidad;
        try {
            cantidad = Double.parseDouble(cantStr);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "La cantidad debe ser un número mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productoId  = idsProductos.get(cboProducto.getSelectedIndex());
        double stock    = getStock(productoId);
        boolean esVenta = rbVenta.isSelected();

        // ── Validaciones de venta ──────────────────────────────────────────────
        if (esVenta) {
            if (stock <= 0) {
                JOptionPane.showMessageDialog(this,
                    "<html><b>No hay stock disponible.</b><br>" +
                    "El kardex de este producto está en 0.<br>" +
                    "Debe registrar una compra primero.</html>",
                    "Stock Insuficiente", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cantidad > stock) {
                JOptionPane.showMessageDialog(this,
                    String.format(
                        "<html><b>Cantidad supera el stock disponible.</b><br>" +
                        "Stock actual: <b>%.2f</b><br>" +
                        "Cantidad solicitada: <b>%.2f</b></html>",
                        stock, cantidad),
                    "Stock Insuficiente", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // ── Calcular y registrar ───────────────────────────────────────────────
        double ingreso = esVenta ? 0      : cantidad;
        double salida  = esVenta ? cantidad : 0;
        double total   = esVenta ? stock - cantidad : stock + cantidad;
        String estado  = esVenta ? "V"    : "C";

        consumo.execute(
            "INSERT INTO inv_kardex(kar_producto, kar_actual, kar_ingreso, kar_salida, kar_total, kar_estado) " +
            "VALUES(?,?,?,?,?,?)",
            productoId, stock, ingreso, salida, total, estado);

        JOptionPane.showMessageDialog(this,
            String.format(
                "<html><b>%s registrada exitosamente.</b><br>" +
                "Stock anterior: %.2f<br>" +
                "Stock nuevo: <b>%.2f</b></html>",
                esVenta ? "Venta" : "Compra", stock, total));

        limpiar();
        cargarHistorial();
        actualizarStock();
    }

    private void cargarHistorial() {
        modelo.setRowCount(0);
        consumo.query(
            "SELECT k.kar_id, p.pro_nombre, k.kar_fecha, " +
            "       k.kar_actual, k.kar_ingreso, k.kar_salida, k.kar_total, k.kar_estado " +
            "FROM inv_kardex k " +
            "LEFT JOIN inv_producto p ON k.kar_producto = p.pro_id " +
            "ORDER BY k.kar_id DESC"
        ).forEach(r -> modelo.addRow(new Object[]{
            r.get("kar_id"),
            r.get("pro_nombre"),
            r.get("kar_fecha"),
            r.get("kar_actual"),
            r.get("kar_ingreso"),
            r.get("kar_salida"),
            r.get("kar_total"),
            "V".equals(r.get("kar_estado")) ? "Venta" : "Compra"
        }));
    }

    private void limpiar() {
        txtCantidad.setText("");
        rbCompra.setSelected(true);
        tabla.clearSelection();
    }
}
