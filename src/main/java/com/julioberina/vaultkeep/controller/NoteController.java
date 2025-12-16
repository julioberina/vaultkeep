package com.julioberina.vaultkeep.controller;

import com.julioberina.vaultkeep.model.Note;
import com.julioberina.vaultkeep.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

	@GetMapping("{id}")
	public ResponseEntity<Note> getNoteById(@PathVariable("id") Long id, Authentication authentication) {
		// Security Check: Fetch the note ONLY if it belongs to the logged-in user.
		// If the ID exists but belongs to someone else, this returns empty (404 Not Found).
		Note note = noteRepository.findByIdAndOwner(id, authentication.getName())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

		return ResponseEntity.ok(note);
	}

	@GetMapping("search")
	public List<Note> search(@RequestParam String query) {
		return noteRepository.findByContentContaining(query);
	}
}
