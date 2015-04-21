package ar.edu.um.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ar.edu.um.dao.Alumno;
import ar.edu.um.dao.Curso;
import ar.edu.um.dao.Inscripcion;
import ar.edu.um.service.AlumnosService;
import ar.edu.um.service.InscripcionesService;

@Controller
public class InscripcionesController {

	private InscripcionesService inscripcionesService;
	private AlumnosService alumnosService;
	
	@Autowired
	public void setInscripcionesService(InscripcionesService inscripcionesService) {
		this.inscripcionesService = inscripcionesService;
	}

	
	@RequestMapping("/registro")
	public String create(Model model) {
		model.addAttribute("curso", new Curso());
		return "registro";
	}
	
	@RequestMapping(value="/doregistro", method=RequestMethod.POST)
	public String doCreate(Model model, @Valid Alumno alumno, Inscripcion inscripcion, BindingResult result) {
		if (result.hasErrors()){
			System.out.println("no se valido el formulario");
			return "registro";
		}
		/* habría que llenar la entidad usuario y a la vez la entidad inscripcion. Como hacer? */
		alumnosService.create(alumno);
		inscripcionesService.create(inscripcion);

		return "ok";
	}
	
}