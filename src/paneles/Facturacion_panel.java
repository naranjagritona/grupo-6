package paneles;

import clases.Usuario;

import com.google.gson.*;

import servicios.Facturacion_servicio;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// consultamos las cuotas y preparamos las operaciones de pago y sus comprobantes
public class Facturacion_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Usuario usuario;
    private final JTextField buscar = new JTextField(25);
    private final JLabel resumen = new JLabel("Cargando facturación…");
    private final JLabel estado = new JLabel(" ");
    private JsonArray cuotas = new JsonArray(),
            pagos = new JsonArray(),
            pendientes = new JsonArray();
    private String periodo = "", importe = "0";
    private int consultaHistorial = 0;
    private final DefaultTableModel mc = modelo("Alumno", "Curso", "Mes", "Estado"),
            mh = modelo("Mes", "Importe", "Medio", "Estado", "Correo"),
            mp = modelo("Alumno", "Mes", "Importe", "Persona que hizo el trámite");
    private final JTable tc = new JTable(mc), th = new JTable(mh), tp = new JTable(mp);
    private final JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    // preparamos las tablas y mostramos la verificación de efectivo solo al administrador.
    public Facturacion_panel(Usuario usuario) {
        this.usuario = usuario;
        servicios.Auditoria_servicio.registrar(
                "ABRIR MODULO", "Facturación", "Consulta del módulo");
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(Color.WHITE);
        JPanel cabecera = new JPanel(new BorderLayout(8, 10));
        JLabel titulo = new JLabel("Facturación");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(resumen, BorderLayout.CENTER);
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Buscar:"));
        filtro.add(buscar);
        JButton consultar = new JButton("Buscar / Actualizar");
        consultar.addActionListener(e -> cargar());
        buscar.addActionListener(e -> cargar());
        filtro.add(consultar);
        cabecera.add(filtro, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);
        for (JTable t : new JTable[] {tc, th, tp}) {
            t.setRowHeight(29);
            t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            t.getTableHeader().setReorderingAllowed(false);
        }
        JPanel historial = new JPanel(new BorderLayout());
        historial.add(
                new JLabel("Pagos y facturas de la inscripción seleccionada"), BorderLayout.NORTH);
        historial.add(tablaConEncabezado(th));
        JSplitPane separador =
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablaConEncabezado(tc), historial);
        separador.setResizeWeight(.55);
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Cuotas y facturas", separador);
        if (servicios.Permisos_servicio.gestion(usuario)) {
            JPanel efectivo = new JPanel(new BorderLayout());
            efectivo.add(tablaConEncabezado(tp));
            JButton confirmar = new JButton("Confirmar recepción del efectivo");
            confirmar.addActionListener(e -> confirmarEfectivo());
            efectivo.add(confirmar, BorderLayout.SOUTH);
            pestanas.addTab("Efectivo pendiente", efectivo);
        }
        add(pestanas);
        tc.getSelectionModel()
                .addListSelectionListener(
                        e -> {
                            if (!e.getValueIsAdjusting()) cargarHistorial();
                        });
        if (servicios.Permisos_servicio.gestion(usuario)) {
            boton("Modificar cuota", this::modificarCuota);
        }
        boton("Pagar cuota", this::pagar);
        boton("Ver factura PDF", this::verFactura);
        boton("Reenviar correo", this::reenviar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(estado, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
        cargar();
    }

    private static JScrollPane tablaConEncabezado(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setColumnHeaderView(tabla.getTableHeader());
        return scroll;
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    public static String texto(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private String dinero(String valor) {
        try {
            return NumberFormat.getCurrencyInstance(Locale.of("es", "AR"))
                    .format(new BigDecimal(valor));
        } catch (Exception e) {
            return valor;
        }
    }

    private void boton(String titulo, Runnable accion) {
        JButton b = new JButton(titulo);
        b.addActionListener(
                e -> {
                    servicios.Auditoria_servicio.registrar("ACCION", "Facturación", titulo);
                    accion.run();
                });
        botones.add(b);
    }

    // muestra el motivo por el que no se pudo completar la operación
    private void error(Throwable e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        String mensaje = causa.getMessage();
        if (causa instanceof java.io.IOException)
            mensaje = "No se pudo comunicar con la web. Revisá la conexión a Internet.";
        JOptionPane.showMessageDialog(this, mensaje, "Facturación", JOptionPane.ERROR_MESSAGE);
    }

    // esperamos la respuesta de la web sin bloquear la ventana y avisamos si hay un error.
    private <T> void ejecutar(Callable<T> tarea, Consumer<T> listo) {
        estado.setText("Procesando…");
        for (Component c : botones.getComponents()) c.setEnabled(false);
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            protected void done() {
                try {
                    listo.accept(get());
                    estado.setText(" ");
                } catch (Exception e) {
                    estado.setText("No se pudo completar la operación.");
                    error(e);
                } finally {
                    for (Component c : botones.getComponents()) c.setEnabled(true);
                }
            }
        }.execute();
    }

    // la web nos devuelve el mes, el importe y las cuotas que puede ver esta cuenta.
    private void cargar() {
        String q = buscar.getText().trim();
        servicios.Auditoria_servicio.registrar(
                "CONSULTAR", "Facturación", q.isEmpty() ? "Actualizar listado" : "Buscar: " + q);
        ejecutar(
                () -> Facturacion_servicio.listar(q),
                r -> {
                    periodo = texto(r, "periodo");
                    importe = texto(r, "importe");
                    resumen.setText("Mes: " + periodo + "   ·   Cuota mensual: " + dinero(importe));
                    cuotas = r.getAsJsonArray("inscripciones");
                    pendientes = r.getAsJsonArray("pendientes");
                    mc.setRowCount(0);
                    mh.setRowCount(0);
                    pagos = new JsonArray();
                    mp.setRowCount(0);
                    for (JsonElement e : cuotas) {
                        JsonObject c = e.getAsJsonObject();
                        String situacion = texto(c, "estado_pago");
                        mc.addRow(
                                new Object[] {
                                    texto(c, "nombre_alumno"),
                                    texto(c, "nombre_curso"),
                                    periodo,
                                    situacion.isEmpty() ? "Sin pago" : situacion
                                });
                    }
                    for (JsonElement e : pendientes) {
                        JsonObject p = e.getAsJsonObject();
                        mp.addRow(
                                new Object[] {
                                    texto(p, "nombre_alumno"),
                                    texto(p, "periodo"),
                                    dinero(texto(p, "importe")),
                                    texto(p, "tramitante")
                                });
                    }
                    if (mc.getRowCount() > 0) tc.setRowSelectionInterval(0, 0);
                });
    }

    // tomamos la inscripción seleccionada antes de abrir el formulario de pago.
    private JsonObject cuota() {
        int r = tc.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná una inscripción.");
            return null;
        }
        return cuotas.get(tc.convertRowIndexToModel(r)).getAsJsonObject();
    }

    // una factura en efectivo no se puede abrir ni reenviar hasta confirmar la recepción.
    private JsonObject pago() {
        int r = th.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un pago del historial.");
            return null;
        }
        JsonObject p = pagos.get(th.convertRowIndexToModel(r)).getAsJsonObject();
        if (!texto(p, "estado").equals("Confirmado")) {
            JOptionPane.showMessageDialog(
                    this,
                    "El efectivo está pendiente de recepción. La factura estará disponible después"
                            + " de confirmar.");
            return null;
        }
        return p;
    }

    // usamos un número de consulta para no mezclar respuestas al cambiar rápido de alumno.
    private void cargarHistorial() {
        int r = tc.getSelectedRow();
        int version = ++consultaHistorial;
        if (r < 0) {
            mh.setRowCount(0);
            return;
        }
        int id =
                cuotas.get(tc.convertRowIndexToModel(r))
                        .getAsJsonObject()
                        .get("id_inscripcion")
                        .getAsInt();
        ejecutar(
                () -> Facturacion_servicio.historial(id),
                lista -> {
                    if (version != consultaHistorial) return;
                    pagos = lista;
                    mh.setRowCount(0);
                    for (JsonElement e : lista) {
                        JsonObject p = e.getAsJsonObject();
                        mh.addRow(
                                new Object[] {
                                    texto(p, "periodo"),
                                    dinero(texto(p, "importe")),
                                    texto(p, "metodo"),
                                    texto(p, "estado"),
                                    texto(p, "correo_estado")
                                });
                    }
                    if (mh.getRowCount() > 0) th.setRowSelectionInterval(0, 0);
                });
    }

    // el nombre y el mes vienen de la base; el formulario no permite modificarlos.
    private void modificarCuota() {
        if (!servicios.Permisos_servicio.gestion(usuario)) {
            return;
        }
        String valor = JOptionPane.showInputDialog(this, "Importe general de la cuota:", importe);
        if (valor == null) {
            return;
        }
        valor = valor.trim().replace(',', '.');
        if (!valor.matches("[0-9]{1,8}(\\.[0-9]{1,2})?") || new BigDecimal(valor).signum() <= 0) {
            JOptionPane.showMessageDialog(
                    this, "Ingresá un importe mayor que cero, con hasta dos decimales.");
            return;
        }
        JsonObject datos = new JsonObject();
        datos.addProperty("importe", valor);
        operar("guardar_importe", datos);
    }

    // prepara y envía los datos de la cuota mensual seleccionada
    private void pagar() {
        JsonObject c = cuota();
        if (c == null) return;
        String actual = texto(c, "estado_pago");
        if (!actual.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Ya existe un pago o una solicitud pendiente para este mes.");
            return;
        }
        if (!java.util.List.of("Pendiente", "Confirmada").contains(texto(c, "estado"))) {
            JOptionPane.showMessageDialog(this, "Esta inscripción no está disponible para pagar.");
            return;
        }
        JTextField nombre = new JTextField(texto(c, "nombre_alumno"));
        nombre.setEditable(false);
        JTextField mes = new JTextField(periodo);
        mes.setEditable(false);
        JTextField dni = new JTextField(), correo = new JTextField();
        JComboBox<String> medio = new JComboBox<>(new String[] {"Tarjeta", "Efectivo"});
        JCheckBox confirmar = new JCheckBox("Confirmo los datos de esta cuota");
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        Object[][] campos = {
            {"Alumno", nombre},
            {"Mes", mes},
            {"Importe", new JLabel(dinero(importe))},
            {"Medio de pago", medio},
            {"DNI del titular", dni},
            {"Correo para recibir el PDF", correo},
            {"Confirmación", confirmar}
        };
        for (Object[] par : campos) {
            form.add(new JLabel((String) par[0]));
            form.add((Component) par[1]);
        }
        while (JOptionPane.showConfirmDialog(
                        this,
                        form,
                        "Pagar cuota mensual",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            if (!confirmar.isSelected()
                    || !dni.getText().matches("[0-9]{7,10}")
                    || !correo.getText().contains("@")) {
                JOptionPane.showMessageDialog(this, "Completá DNI, correo y confirmación.");
                continue;
            }
            JsonObject datos = new JsonObject();
            datos.addProperty("id_inscripcion", c.get("id_inscripcion").getAsInt());
            datos.addProperty("periodo", periodo);
            datos.addProperty("metodo", (String) medio.getSelectedItem());
            datos.addProperty("dni", dni.getText().trim());
            datos.addProperty("email", correo.getText().trim());
            datos.addProperty("confirmacion", "1");
            operar("pagar", datos);
            break;
        }
    }

    // enviamos la operación y actualizamos el listado con el estado que guardó la web.
    private void operar(String accion, JsonObject datos) {
        ejecutar(
                () -> Facturacion_servicio.operar(accion, datos),
                r -> {
                    servicios.Auditoria_servicio.registrar(
                            "FACTURACION", "Facturación", "Operación: " + accion);
                    JOptionPane.showMessageDialog(this, texto(r, "mensaje"));
                    cargar();
                });
    }

    // quien recibe el dinero escribe su nombre y confirma que el efectivo fue entregado.
    private void confirmarEfectivo() {
        if (!servicios.Permisos_servicio.gestion(usuario)) return;
        int fila = tp.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná el efectivo pendiente.");
            return;
        }
        JsonObject p = pendientes.get(fila).getAsJsonObject();
        JTextField titular = new JTextField(texto(p, "tramitante"));
        titular.setEditable(false);
        JTextField atendio = new JTextField();
        JCheckBox recibido = new JCheckBox("Confirmo que recibí el dinero");
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Persona que hizo el trámite"));
        form.add(titular);
        form.add(new JLabel("Recibido por"));
        form.add(atendio);
        form.add(new JLabel("Verificación"));
        form.add(recibido);
        while (JOptionPane.showConfirmDialog(
                        this,
                        form,
                        "Confirmar efectivo · " + dinero(texto(p, "importe")),
                        JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            if (atendio.getText().isBlank() || !recibido.isSelected()) {
                JOptionPane.showMessageDialog(
                        this, "Escribí quién recibió el dinero y confirmá la recepción.");
                continue;
            }
            JsonObject datos = new JsonObject();
            datos.addProperty("id_pago", p.get("id_pago").getAsInt());
            datos.addProperty("id_inscripcion", p.get("id_inscripcion").getAsInt());
            datos.addProperty("atendido_por", atendio.getText().trim());
            datos.addProperty("confirmacion_efectivo", "1");
            operar("confirmar_efectivo", datos);
            break;
        }
    }

    // descargamos el PDF generado por la web y lo mostramos dentro de la aplicación.
    private void verFactura() {
        JsonObject p = pago();
        if (p == null) return;
        int id = p.get("id_factura").getAsInt();
        ejecutar(
                () -> {
                    byte[] pdf = Facturacion_servicio.factura(id);
                    return new Object[] {pdf, FacturaPDF_panel.renderizar(pdf)};
                },
                r -> {
                    FacturaPDF_panel.mostrar(this, (byte[]) r[0], (JPanel) r[1]);
                    servicios.Auditoria_servicio.registrar(
                            "VER PDF", "Facturación", "Factura " + id);
                });
    }

    // el correo se reenvía a la dirección que quedó registrada al pagar.
    private void reenviar() {
        JsonObject p = pago();
        if (p == null) return;
        JsonObject datos = new JsonObject();
        datos.addProperty("id_factura", p.get("id_factura").getAsInt());
        operar("reenviar", datos);
    }
}
