package paneles;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class Inscripcion_panel extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFecha;
	private JTextField txtImporteCuota;
	private JTextField txtBuscar;
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Inscripcion_panel frame = new Inscripcion_panel();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Inscripcion_panel() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1150, 680); //Tamaño de la pantalla
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panelTop = new JPanel();
		panelTop.setPreferredSize(new Dimension(10, 50));
		panelTop.setBackground(new Color(31, 78, 140));
		contentPane.add(panelTop, BorderLayout.NORTH);
		panelTop.setLayout(new BorderLayout(0, 0));
		
		//Titulo de la academia
		JLabel lblTituloAcademia = new JLabel(" Academia Integral XXI");
		lblTituloAcademia.setForeground(Color.WHITE);
		lblTituloAcademia.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelTop.add(lblTituloAcademia, BorderLayout.CENTER);
		
		JPanel panelMenu = new JPanel();
		panelMenu.setBackground(new Color(0, 0, 64));
		panelMenu.setPreferredSize(new Dimension(170, 10));
		contentPane.add(panelMenu, BorderLayout.WEST);
		panelMenu.setLayout(null);
		
		
		JLabel lblMenu = new JLabel("Menu");
		lblMenu.setForeground(new Color(255, 255, 255));
		lblMenu.setBackground(new Color(255, 255, 255));
		lblMenu.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblMenu.setBounds(49, 0, 60, 57);
		panelMenu.add(lblMenu);
		
		//BOTONES DEL MENU
		JButton btnInscripciones = new JButton("Inscripciones");
		btnInscripciones.setBounds(15, 61, 140, 35);
		btnInscripciones.setBackground(new Color(37, 99, 235)); // Botón activo destacado
		btnInscripciones.setForeground(Color.WHITE);
		btnInscripciones.setFont(new Font("Tahoma", Font.BOLD, 12));
		panelMenu.add(btnInscripciones);
		
		JButton btnCursos = new JButton("Cursos");
		btnCursos.setBounds(15, 107, 140, 35);
		btnCursos.setBackground(new Color(24, 61, 110)); // Tono secundario en armonía
		btnCursos.setForeground(new Color(203, 213, 225));
		btnCursos.setFont(new Font("Tahoma", Font.BOLD, 12));
		panelMenu.add(btnCursos);
		
		JButton btnMaterias = new JButton("Materias");
		btnMaterias.setBounds(15, 152, 140, 35);
		btnMaterias.setBackground(new Color(24, 61, 110));
		btnMaterias.setForeground(new Color(203, 213, 225));
		btnMaterias.setFont(new Font("Tahoma", Font.BOLD, 12));
		panelMenu.add(btnMaterias);
		
		JButton btnCalificaciones = new JButton("Calificaciones");
		btnCalificaciones.setBounds(15, 197, 140, 35);
		btnCalificaciones.setBackground(new Color(24, 61, 110));
		btnCalificaciones.setForeground(new Color(203, 213, 225));
		btnCalificaciones.setFont(new Font("Tahoma", Font.BOLD, 12));
		panelMenu.add(btnCalificaciones);
		
		//Divide en dos para el formulario y otro para la tabla 
		//que muestra los datos de las inscripciones
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(420);
        contentPane.add(splitPane, BorderLayout.CENTER);
		
		JPanel panelGestionInscripcion = new JPanel();
		panelGestionInscripcion.setForeground(new Color(30, 58, 138));
		splitPane.setLeftComponent(panelGestionInscripcion);
		panelGestionInscripcion.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Gestión de Inscripciones");
		lblNewLabel.setForeground(new Color(0, 128, 192));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblNewLabel.setBounds(95, 24, 212, 21);
		panelGestionInscripcion.add(lblNewLabel);
		
		JLabel lblAlumno = new JLabel("Alumno:");
		lblAlumno.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAlumno.setForeground(new Color(51, 65, 85));
		lblAlumno.setBounds(10, 86, 110, 14);
		panelGestionInscripcion.add(lblAlumno);
		
		JLabel lblCurso = new JLabel("Curso / Materia:");
		lblCurso.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCurso.setForeground(new Color(51, 65, 85));
		lblCurso.setBounds(10, 126, 110, 14);
		panelGestionInscripcion.add(lblCurso);
		
		JLabel lblFecha = new JLabel("Fecha:");
		lblFecha.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblFecha.setForeground(new Color(51, 65, 85));
		lblFecha.setBounds(10, 166, 110, 14);
		panelGestionInscripcion.add(lblFecha);
		
		JLabel lblEstado = new JLabel("Estado:");
		lblEstado.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblEstado.setForeground(new Color(51, 65, 85));
		lblEstado.setBounds(10, 206, 110, 14);
		panelGestionInscripcion.add(lblEstado);
		
		JLabel lblImporte = new JLabel("Importe Cuota ($):");
		lblImporte.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblImporte.setForeground(new Color(51, 65, 85));
		lblImporte.setBounds(10, 246, 117, 21);
		panelGestionInscripcion.add(lblImporte);
		
		JComboBox cmbAlumno = new JComboBox();
		cmbAlumno.setBounds(140, 84, 250, 22);
		panelGestionInscripcion.add(cmbAlumno);
		
		JComboBox cmbCurso = new JComboBox();
		cmbCurso.setBounds(140, 124, 250, 22);
		panelGestionInscripcion.add(cmbCurso);
		
		textFecha = new JTextField();
		textFecha.setBounds(140, 165, 250, 22);
		panelGestionInscripcion.add(textFecha);
		textFecha.setColumns(10);
		
		txtImporteCuota = new JTextField();
		txtImporteCuota.setColumns(10);
		txtImporteCuota.setBounds(137, 247, 250, 22);
		panelGestionInscripcion.add(txtImporteCuota);
		
		JComboBox cmbEstado = new JComboBox();
		cmbEstado.setBounds(140, 204, 250, 22);
		panelGestionInscripcion.add(cmbEstado);
		
		JButton btnRegistrar = new JButton("Registrar");
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnRegistrar.setForeground(Color.WHITE);
		btnRegistrar.setFont(new Font("Arial", Font.BOLD, 12));
		btnRegistrar.setFocusPainted(false);
		btnRegistrar.setBackground(new Color(37, 99, 235));
		btnRegistrar.setBounds(48, 311, 100, 32);
		panelGestionInscripcion.add(btnRegistrar);
		
		JButton btnModificar = new JButton("Modificar");
		btnModificar.setForeground(Color.WHITE);
		btnModificar.setFont(new Font("Arial", Font.BOLD, 12));
		btnModificar.setFocusPainted(false);
		btnModificar.setBackground(new Color(59, 130, 246));
		btnModificar.setBounds(161, 311, 100, 32);
		panelGestionInscripcion.add(btnModificar);
		
		JButton btnDarDeBaja = new JButton("Dar de baja");
		btnDarDeBaja.setForeground(Color.WHITE);
		btnDarDeBaja.setFont(new Font("Arial", Font.BOLD, 12));
		btnDarDeBaja.setFocusPainted(false);
		btnDarDeBaja.setBackground(new Color(100, 116, 139));
		btnDarDeBaja.setBounds(274, 311, 100, 32);
		panelGestionInscripcion.add(btnDarDeBaja);
		
		JPanel panelTablaInscripcion = new JPanel();
		splitPane.setRightComponent(panelTablaInscripcion);
		panelTablaInscripcion.setLayout(new BorderLayout(0, 0));
		
        JPanel panelFiltros = new JPanel();
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setPreferredSize(new Dimension(10, 55));
        panelTablaInscripcion.add(panelFiltros, BorderLayout.NORTH);
        panelFiltros.setLayout(null);
        
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblBuscar.setBounds(10, 11, 55, 33);
        panelFiltros.add(lblBuscar);
        
        txtBuscar = new JTextField();
        txtBuscar.setBounds(75, 18, 180, 22);
        panelFiltros.add(txtBuscar);
        txtBuscar.setColumns(10);
        
        JLabel lblFiltrarEstado = new JLabel("Estado:");
        lblFiltrarEstado.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblFiltrarEstado.setBounds(265, 11, 55, 33);
        panelFiltros.add(lblFiltrarEstado);
        
        JComboBox cmbFiltrarEstado = new JComboBox();
        cmbFiltrarEstado.setBounds(319, 18, 100, 22);
        panelFiltros.add(cmbFiltrarEstado);
        
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Arial", Font.BOLD, 11));
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.setBackground(new Color(30, 58, 138));
        btnFiltrar.setBounds(429, 17, 99, 25);
        panelFiltros.add(btnFiltrar);
        
        JScrollPane scrollPane = new JScrollPane();
        panelTablaInscripcion.add(scrollPane, BorderLayout.CENTER);
        
        table = new JTable();
        scrollPane.setColumnHeaderView(table);

	}
}
