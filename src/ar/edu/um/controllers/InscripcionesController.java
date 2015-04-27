package ar.edu.um.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ar.edu.um.dao.Alumno;
import ar.edu.um.dao.Curso;
import ar.edu.um.dao.Documento;
import ar.edu.um.dao.Inscripcion;
import ar.edu.um.service.AlumnosService;
import ar.edu.um.service.CursosService;
import ar.edu.um.service.DocumentosService;
import ar.edu.um.service.InscripcionesService;

@Controller
public class InscripcionesController {

	private AlumnosService alumnosService;
	private DocumentosService documentosService;
	private CursosService cursosService;
	private InscripcionesService inscripcionesService;
	

	 @Autowired
	 private JavaMailSender mailSender;
	
	@Autowired
	public void setAlumnosService(AlumnosService alumnosService) {
		this.alumnosService = alumnosService;
	}
	
	@Autowired
	public void setDocumentosService(DocumentosService documentosService) {
		this.documentosService = documentosService;
	}
	
	@Autowired
	public void setCursosService(CursosService cursosService) {
		this.cursosService = cursosService;
	}

	@Autowired
	public void setInscripcionesService(InscripcionesService inscripcionesService) {
		this.inscripcionesService = inscripcionesService;
	}

	@RequestMapping("/registro")
	public String create(@RequestParam("cur_id") String cur_id, Model model) {
		Curso curso = new Curso();
		curso = cursosService.getCurso(Integer.parseInt(cur_id));
		model.addAttribute("cur_titulo", curso.getCur_titulo());
		model.addAttribute("cur_id", curso.getCur_id());
		List<Documento> documentos = documentosService.getCurrent();
		model.addAttribute("documentos", documentos);
		return "registro";
		
	}
	

    /*---------- VERIFICACION DNI ----------*/
   
	@RequestMapping("/verificacion")
	public String verificaRegistro(@RequestParam("cur_id") String cur_id, Model model) {
		
		List<Documento> documentos = documentosService.getCurrent();
		model.addAttribute("documentos", documentos);
		model.addAttribute("cur_id", cur_id);
		
		System.out.println("CUR_ID: " + cur_id);

		return "verificacion";
		
	}
	
    /*---------- CONTROL ----------*/
   
    @RequestMapping(value="/control", method=RequestMethod.POST)
    public String control(Model model, @RequestParam("cur_id") String cur_id, @RequestParam("alu_doc_id") String alu_doc_id, @RequestParam("alu_dni") String alu_dni){
       
    	model.addAttribute("cur_id", cur_id);
		model.addAttribute("alu_doc_id", alu_doc_id);
		model.addAttribute("alu_dni", alu_dni);
		
		System.out.println("CUR_ID: " + cur_id + " ALU_DNI: " + alu_dni);
		
		Alumno alumno = new Alumno();
		
		alumno = alumnosService.getAlumno(Integer.parseInt(alu_dni), Integer.parseInt(alu_doc_id));
       
		System.out.println("ALU: " + alumno + "\n");
		
		System.out.println("DNI " + alumno.getAlu_dni());
		
		if (alumno.getAlu_dni() == 0){
        	System.out.println("No se encontró el documento en la BD");
            return "registro?cur_id=" + cur_id;
        }else{
        	System.out.println("Sí se encontró el documento en la BD");
            return "confirmacion";
        }
       
    }
	
	@RequestMapping(value="/crearegistro", method=RequestMethod.POST)
	public String creaRegistro(Model model, @RequestParam String dia, @RequestParam String mes, @RequestParam String anio, @RequestParam String cur_id, @RequestParam String cur_titulo , @Valid Alumno alumno, BindingResult result) {
		if (result.hasErrors()){
			System.out.println("no se valido el formulario");
			List<ObjectError> errors = result.getAllErrors();
			
			for (Object error: errors) {
				System.out.println(error);
			}
			
			return "registro";
		}
		
		String fecha = anio + "-" + mes + "-" + dia;
		alumno.setAlu_fechanac(fecha);
		//System.out.println(fecha);
		//System.out.println(alumno);
		alumnosService.create(alumno);
		
		model.addAttribute("alu_dni", alumno.getAlu_dni());
		model.addAttribute("alu_doc_id", alumno.getAlu_doc_id());
		model.addAttribute("cur_id", cur_id);
		model.addAttribute("cur_titulo", cur_titulo);
		
		return "confirmacion";
	}
	
	@RequestMapping(value="/registrado", method=RequestMethod.POST)
	public String confirmaRegistro(Model model, @RequestParam("alu_dni") String alu_dni, @RequestParam("alu_doc_id") String alu_doc_id,  @RequestParam("cur_id") String cur_id) {

		Inscripcion inscripcion = new Inscripcion();

		SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
	    String strFecha = fecha.format(now);
	    
	    Alumno alumno = new Alumno();
	    alumno = alumnosService.getAlumno(Integer.parseInt(alu_dni), Integer.parseInt(alu_doc_id));
	    
	    Documento documento = new Documento();
	    documento = documentosService.getDocumento(Integer.parseInt(alu_doc_id));
	    
	    Curso curso = new Curso();
	    curso = cursosService.getCurso(Integer.parseInt(cur_id));

		inscripcion.setIns_alu_id(alumno.getAlu_id());
		inscripcion.setIns_cur_id(Integer.parseInt(cur_id));
		inscripcion.setIns_fecha(strFecha);
		
		System.out.println(inscripcion);
		
		inscripcionesService.create(inscripcion);
		
		
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo("ivankuzel@gmail.com");
		email.setSubject("Nueva inscripción a Curso");
		
		String texto = "Se ha registrado una nueva inscripción con los siguientes datos:\n\n" + 
		"Datos del Alumno:\n" + "Tipo de Documento: " + documento.getDoc_nombre() + "\nNro de Documento: " +
		alumno.getAlu_dni() + "\nNombre y Apellido: " + alumno.getAlu_nombre() + " " + alumno.getAlu_apellido() + 
		"\nDomicilio: " + alumno.getAlu_domicilio() + "\nEmail: " + alumno.getAlu_email() + "\nFecha de Nacimiento: "
		+ alumno.getAlu_fechanac() + "\nTelefono: " + alumno.getAlu_telefono() + "\n\n" + 
		"Datos del Curso en el cual se inscribió:\n" + "Nombre: " + curso.getCur_titulo() + "\nDescripción: "
		+ curso.getCur_descripcion() + "\nProfesor: " + curso.getCur_profesor() + "\nFecha de Inicio: " + curso.getCur_inicio() + 
		"\nDuración: " + curso.getCur_duracion() + "horas";
		
		email.setText(texto);
		
		mailSender.send(email);
		
		return "registrado";
	}
	
}