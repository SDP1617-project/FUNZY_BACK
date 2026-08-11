package com.sdp1617.backend.letter.repository;

import com.sdp1617.backend.letter.entity.ReceivedLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReceivedLetterRepository extends JpaRepository<ReceivedLetter, Long>, JpaSpecificationExecutor<ReceivedLetter> {
}
