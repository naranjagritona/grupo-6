import clases.Curso;
import conexion.Conexion;
import dao.Curso_dao;
import java.sql.*;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.List;

public class PruebaCursos {
 static void ok(boolean c,String m){if(!c)throw new AssertionError(m);System.out.println("OK: "+m);}
 static void rechaza(Runnable r,String m){try{r.run();}catch(IllegalArgumentException|IllegalStateException e){System.out.println("OK: "+m);return;}throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  // Las tablas temporales ocultan las reales solo durante esta conexión de prueba.
  try(Connection real=Conexion.getConnection()){
   try(Statement s=real.createStatement()){
    s.execute("CREATE TEMPORARY TABLE periodo_lectivo(id_periodo_lectivo INT PRIMARY KEY,nombre VARCHAR(100))");
    s.execute("CREATE TEMPORARY TABLE materia(id_materia INT PRIMARY KEY,nombre_materia VARCHAR(100))");
    s.execute("CREATE TEMPORARY TABLE docente(id_docente INT PRIMARY KEY,nombre VARCHAR(100),apellido VARCHAR(100),id_usuario INT)");
    s.execute("CREATE TEMPORARY TABLE curso(id_curso INT PRIMARY KEY AUTO_INCREMENT,id_periodo INT,id_materia INT,id_docente INT,año VARCHAR(10),modalidad VARCHAR(20),turno VARCHAR(20),cupo_maximo INT,fecha_inicio DATE,fecha_fin DATE,estado VARCHAR(20)) ENGINE=InnoDB");
    s.execute("CREATE TEMPORARY TABLE curso_materia(id_curso INT,id_materia INT CHECK(id_materia IN(1,2,3)),PRIMARY KEY(id_curso,id_materia)) ENGINE=InnoDB");
    s.execute("CREATE TEMPORARY TABLE inscripcion(id_inscripcion INT PRIMARY KEY,id_curso INT,estado VARCHAR(20))");
    s.execute("CREATE TEMPORARY TABLE columna_notas(id_columna_notas INT PRIMARY KEY,id_curso INT)");
    s.execute("INSERT INTO periodo_lectivo VALUES(1,'2026')");s.execute("INSERT INTO materia VALUES(1,'Lengua'),(2,'Matemática'),(3,'Historia')");s.execute("INSERT INTO docente VALUES(1,'Ana','Castillo',1)");
    s.execute("INSERT INTO curso VALUES(1,1,1,1,'1','Presencial','tarde',30,'2026-03-02','2026-12-18','Activo')");
    s.execute("INSERT INTO curso_materia VALUES(1,1)");s.execute("INSERT INTO inscripcion VALUES(1,1,'Confirmada')");s.execute("INSERT INTO columna_notas VALUES(1,1)");
   }
   Connection prestada=(Connection)Proxy.newProxyInstance(PruebaCursos.class.getClassLoader(),new Class[]{Connection.class},(obj,m,p)->{if(m.getName().equals("close"))return null;try{return m.invoke(real,p);}catch(InvocationTargetException e){throw e.getCause();}});
   Curso_dao dao=new Curso_dao(){protected Connection abrirConexion(){return prestada;}};
   Curso c=new Curso();c.setAño(2);c.setId_periodo(1);c.setId_docente(1);c.setModalidad("Presencial");c.setTurno("tarde");c.setCupo_maximo(30);c.setFecha_inicio(LocalDate.of(2026,3,2));c.setFecha_fin(LocalDate.of(2026,12,18));c.setEstado("Activo");
   dao.guardar(c,List.of(1,2,2));ok(dao.materiasDelCurso(c.getId_curso()).equals(List.of(1,2)),"guarda varias materias sin duplicarlas");
   dao.guardar(c,List.of(2,3));ok(dao.materiasDelCurso(c.getId_curso()).equals(List.of(2,3)),"edita las materias seleccionadas");
   rechaza(()->dao.guardar(c,List.of()),"exige al menos una materia");
   rechaza(()->dao.guardar(c,List.of(2,999)),"revierte si falla una asociación de materias");
   ok(dao.materiasDelCurso(c.getId_curso()).equals(List.of(2,3)),"la transacción conserva las materias anteriores ante un error");
   Curso original=dao.obtenerPorId(1);rechaza(()->dao.guardar(original,List.of(2)),"conserva la materia que ya tiene evaluaciones");
   c.setAño(1);rechaza(()->dao.guardar(c,List.of(2,3)),"evita dos cursos del mismo año y período");c.setAño(2);
   dao.cancelar(c.getId_curso());ok(dao.obtenerPorId(c.getId_curso()).getEstado().equals("Cancelado"),"cancelar conserva el curso");
   rechaza(()->dao.eliminar(1),"bloquea eliminar desde el DAO");
   try(Statement s=real.createStatement();ResultSet rs=s.executeQuery("SELECT COUNT(*) FROM inscripcion")){rs.next();ok(rs.getInt(1)==1,"conserva las inscripciones");}
  }
 }
}
