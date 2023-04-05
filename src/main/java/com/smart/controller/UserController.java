package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.standard.expression.Each;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
//inside portal controller
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// metod for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		model.addAttribute("user", user);
	}

	// dashboard home
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form controller
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	 @PostMapping("user/add-contact") 

	public String processContactForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);

			contact.setUser(user);

			// processing and uploading file

			if (file.isEmpty()) {
				System.out.println("File not found");
				contact.setImage("contact.png");
			} else {
				// file the file to folder and update name in contact
				contact.setImage(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}

			user.getContacts().add(contact);
			userRepository.save(user);

			// success message
//				session.setAttribute("message", new Message("Your contact is added !! Add more..","success"));

			System.out.println(contact);
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
			// message error
//				session.setAttribute("message", new Message("Something went wrong !! Try again..","danger"));
		}
		return "normal/add_contact_form";

	}

	// show contacts
	// per page=5[n]
	// current page = 0[page]
	@GetMapping("/show-contacts")
	public String showContactForm(Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		// send contact list for particular user

		String name = principal.getName();
		System.out.println(name);
		User user = userRepository.getUserByUserName(name);

//		  System.out.println(user);
		List<Contact> contacts = user.getContacts();
		model.addAttribute("contacts", contacts);

//		for (Contact contact : contacts) {
//			System.out.println(contact);
//		}
		// send contact list for particular user

		/*
		 * PAGINATION currentPage-page contact per page - 5 PageRequest pageRequest =
		 * PageRequest.of(page, 5); Page<Contact> contactByUser
		 * =contactRepository.findContactByUser(user.getId(),pageRequest);
		 * 
		 */
//		for (Contact contact : contactByUser) {
//			 System.out.println(contact);
//		}
		model.addAttribute("contacts", contacts);

		return "normal/show-contacts";
	}

	// showing particular contact detail
	@GetMapping("/contact/{cId}")
	public String showContact(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		Optional<Contact> id = contactRepository.findById(cId);
		Contact contact = id.get();

		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// delete-contact handler
	/* @GetMapping("/delete/{cId}") */
	@RequestMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session) {

		
		Contact contact = contactRepository.findById(cId).get();
		contactRepository.delete(contact);
		session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		
		return "redirect:/user/show-contacts";
	}
	//open update-contact handler
	
	/* @RequestMapping(value="/update-contact/{cId}",method = RequestMethod.POST) */
	 @PostMapping("update-contact/{cId}") 
	 
	public String updateContact(@PathVariable("cId") Integer cId,Model model) {
		
		model.addAttribute("title","update Contact");
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	/* @RequestMapping(value="/process-update",method = RequestMethod.POST) */
	@PostMapping("process-update")
	public String updatehandler(@ModelAttribute Contact contact,Principal principal,@RequestParam("profileImage")MultipartFile file,Model model,HttpSession session) {
		try {
			//image
			Contact oldcontact = contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//rewrite
				//delete old photo
				
				 File deleteFile = new ClassPathResource("static/img").getFile();
				 File file1=new File(deleteFile,oldcontact.getImage());
				 file1.delete();
			
				//update new photo
				
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else
			{	
				contact.setImage(oldcontact.getImage());
			}
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is successfully updated", "success"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/show-contacts";
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	
	
	
	
	
	
	
}
