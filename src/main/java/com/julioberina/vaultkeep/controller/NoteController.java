package com.julioberina.vaultkeep.controller;

import com.julioberina.vaultkeep.model.Note;
import com.julioberina.vaultkeep.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("notes")
@RequiredArgsConstructor
public class NoteController {
	private final NoteRepository noteRepository;

	@GetMapping
	public List<Note> getAllNotes() {
		return noteRepository.findAll();
	}

	@PostMapping
	public Note createNote(@RequestBody Note note) {
		return noteRepository.save(note);
	}

	@GetMapping("search")
	public List<Note> search(@RequestParam String query) {
		return noteRepository.findByContentContaining(query);
	}
}
