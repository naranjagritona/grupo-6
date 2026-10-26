package paneles;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

import javax.swing.*;

// mostramos las páginas del PDF y permitimos guardar el comprobante
public final class FacturaPDF_panel {
    private FacturaPDF_panel() {}

    // Convertimos las páginas del PDF en imágenes para poder verlas en Swing.
    public static JPanel renderizar(byte[] pdf) throws Exception {
        JPanel paginas = new JPanel();
        paginas.setLayout(new BoxLayout(paginas, BoxLayout.Y_AXIS));
        paginas.setBackground(Color.GRAY);
        // El PDF se genera en la web; acá solamente mostramos sus páginas.
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            PDFRenderer renderer = new PDFRenderer(documento);
            for (int i = 0; i < documento.getNumberOfPages(); i++) {
                BufferedImage imagen = renderer.renderImageWithDPI(i, 100);
                JLabel pagina = new JLabel(new ImageIcon(imagen));
                pagina.setAlignmentX(Component.CENTER_ALIGNMENT);
                paginas.add(pagina);
            }
        }
        return paginas;
    }

    // Abrimos una ventana aparte para consultar el PDF sin cerrar Facturación.
    public static void mostrar(Component padre, byte[] pdf, JPanel paginas) {
        JDialog ventana =
                new JDialog(
                        SwingUtilities.getWindowAncestor(padre),
                        "Factura PDF",
                        Dialog.ModalityType.MODELESS);
        ventana.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        ventana.add(new JScrollPane(paginas));
        // Elegimos dónde guardar y pedimos confirmación si el archivo ya existe.
        JButton guardar = new JButton("Guardar PDF");
        guardar.addActionListener(
                e -> {
                    JFileChooser selector = new JFileChooser();
                    selector.setSelectedFile(new java.io.File("factura.pdf"));
                    if (selector.showSaveDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                        try {
                            java.nio.file.Path destino = selector.getSelectedFile().toPath();
                            if (Files.exists(destino)
                                    && JOptionPane.showConfirmDialog(
                                                    ventana,
                                                    "¿Reemplazar el archivo existente?",
                                                    "Guardar PDF",
                                                    JOptionPane.YES_NO_OPTION)
                                            != JOptionPane.YES_OPTION) return;
                            Files.write(destino, pdf);
                            servicios.Auditoria_servicio.registrar(
                                    "GUARDAR PDF", "Facturación", "Comprobante guardado");
                        } catch (Exception error) {
                            JOptionPane.showMessageDialog(ventana, "No se pudo guardar el PDF.");
                        }
                    }
                });
        ventana.add(guardar, BorderLayout.SOUTH);
        ventana.setSize(900, 720);
        ventana.setLocationRelativeTo(padre);
        ventana.setVisible(true);
    }
}
