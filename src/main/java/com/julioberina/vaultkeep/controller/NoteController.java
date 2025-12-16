package com.julioberina.vaultkeep.controller;

import com.julioberina.vaultkeep.model.Note;
import com.julioberina.vaultkeep.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("notes")
@RequiredArgsConstructor
public class NoteController {
	private final NoteRepository noteRepository;

	@GetMapping
	public List<Note> getAllNotes(Authentication authentication) {
		return noteRepository.findByOwner(authentication.getName());
	}

	@PostMapping
	public Note createNote(@RequestBody Note note, Authentication authentication) {
		note.setOwner(authentication.getName());
		return noteRepository.save(note);
	}

	// ---------------------------------------------------------
	// 🚨 VULNERABLE ENDPOINT (IDOR) 🚨
	// This method allows User A to read User B's notes by just guessing the ID.
	// It does NOT check if the 'owner' matches the 'authentication.getName()'.
	// ---------------------------------------------------------
	@GetMapping("{id}")
	public Note getNoteById(@PathVariable("id") Long id) {
		return noteRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Note not found"));
	}

	@GetMapping("search")
	public List<Note> search(@RequestParam String query) {
		return noteRepository.findByContentContaining(query);
	}
}
