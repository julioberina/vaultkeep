package com.julioberina.vaultkeep.repository;

import com.julioberina.vaultkeep.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
	List<Note> findByContentContaining(String content);
	List<Note> findByOwner(String owner);
	Optional<Note> findByIdAndOwner(Long id, String owner);
}
