package com.sj.voicebook.member.repository;

import com.sj.voicebook.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {
}
