package org.zerock.Altari.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.Altari.member.entity.MemberEntity;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
}
